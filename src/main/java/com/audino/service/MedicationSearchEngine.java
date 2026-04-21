package com.audino.service;

import com.audino.model.Medication;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class MedicationSearchEngine {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[^a-z0-9]+");
    private static final Pattern NORMALIZE_PATTERN = Pattern.compile("[^a-z0-9\\s]+");

    private final List<Medication> medications;
    private final Map<String, Set<Medication>> medicationsByToken;
    private final Map<Medication, String> searchableTextByMedication;
    private final AhoCorasickAutomaton automaton;

    public MedicationSearchEngine(List<Medication> medications) {
        this.medications = medications == null ? List.of() : List.copyOf(medications);
        this.medicationsByToken = new HashMap<>();
        this.searchableTextByMedication = new HashMap<>();

        Set<String> tokenPatterns = new HashSet<>();
        for (Medication medication : this.medications) {
            String searchable = normalize(composeSearchableText(medication));
            searchableTextByMedication.put(medication, searchable);

            for (String token : tokenize(searchable)) {
                if (token.length() < 2) {
                    continue;
                }
                tokenPatterns.add(token);
                medicationsByToken.computeIfAbsent(token, key -> new LinkedHashSet<>()).add(medication);
            }
        }

        this.automaton = new AhoCorasickAutomaton(tokenPatterns);
    }

    public List<Medication> suggest(String rawInput, int limit) {
        if (medications.isEmpty()) {
            return List.of();
        }

        int safeLimit = limit > 0 ? limit : 8;
        String query = normalize(rawInput);

        if (query.isBlank()) {
            return medications.stream()
                    .sorted(Comparator.comparing(MedicationSearchEngine::genericName, String.CASE_INSENSITIVE_ORDER))
                    .limit(safeLimit)
                    .toList();
        }

        Set<Medication> candidates = new LinkedHashSet<>();
        for (String token : automaton.search(query)) {
            candidates.addAll(medicationsByToken.getOrDefault(token, Set.of()));
        }

        for (Medication medication : medications) {
            String searchable = searchableTextByMedication.getOrDefault(medication, "");
            if (searchable.contains(query)) {
                candidates.add(medication);
            }
        }

        Collection<Medication> scoringPool = candidates.isEmpty() ? medications : candidates;
        List<ScoredMedication> scored = new ArrayList<>();
        for (Medication medication : scoringPool) {
            double score = scoreMedication(medication, query, !candidates.isEmpty());
            if (score >= 0.45d) {
                scored.add(new ScoredMedication(medication, score));
            }
        }

        if (scored.isEmpty()) {
            for (Medication medication : medications) {
                double score = scoreMedication(medication, query, false);
                if (score >= 0.35d) {
                    scored.add(new ScoredMedication(medication, score));
                }
            }
        }

        scored.sort(Comparator
                .comparingDouble(ScoredMedication::score).reversed()
                .thenComparing(sm -> genericName(sm.medication()), String.CASE_INSENSITIVE_ORDER));

        List<Medication> results = new ArrayList<>();
        for (ScoredMedication scoredMedication : scored) {
            results.add(scoredMedication.medication());
            if (results.size() >= safeLimit) {
                break;
            }
        }
        return results;
    }

    public Optional<String> autoCorrect(String rawInput) {
        String query = normalize(rawInput);
        if (query.isBlank()) {
            return Optional.empty();
        }

        for (Medication medication : medications) {
            String generic = normalize(genericName(medication));
            String brand = normalize(brandName(medication));
            String rxNorm = normalize(rxNormCode(medication));
            if (query.equals(generic) || (!brand.isBlank() && query.equals(brand)) || (!rxNorm.isBlank() && query.equals(rxNorm))) {
                return Optional.of(genericName(medication));
            }
        }

        List<Medication> suggestions = suggest(rawInput, 3);
        if (suggestions.isEmpty()) {
            return Optional.empty();
        }

        Medication best = suggestions.get(0);
        double bestScore = scoreMedication(best, query, true);
        double secondScore = suggestions.size() > 1 ? scoreMedication(suggestions.get(1), query, true) : 0d;

        if (bestScore >= 2.2d && (bestScore - secondScore) >= 0.10d) {
            return Optional.of(genericName(best));
        }
        return Optional.empty();
    }

    private double scoreMedication(Medication medication, String query, boolean hasCandidates) {
        String generic = normalize(genericName(medication));
        String brand = normalize(brandName(medication));
        String rxNorm = normalize(rxNormCode(medication));
        String searchable = searchableTextByMedication.getOrDefault(medication, (generic + " " + brand + " " + rxNorm).trim()).trim();

        double score = 0d;
        if (generic.equals(query) || (!brand.isBlank() && brand.equals(query)) || (!rxNorm.isBlank() && rxNorm.equals(query))) {
            score += 4.0d;
        }
        if (generic.startsWith(query) || (!brand.isBlank() && brand.startsWith(query)) || (!rxNorm.isBlank() && rxNorm.startsWith(query))) {
            score += 2.2d;
        }
        if (generic.contains(query) || (!brand.isBlank() && brand.contains(query)) || (!rxNorm.isBlank() && rxNorm.contains(query)) || searchable.contains(query)) {
            score += 1.4d;
        }

        score += maxSimilarity(query, generic, brand, rxNorm, searchable) * 2.0d;
        score += cosineSimilarity(embedding(query), embedding(searchable)) * 2.1d;

        Set<String> queryTokens = new HashSet<>(tokenize(query));
        Set<String> medicationTokens = new HashSet<>(tokenize(searchable));
        if (!queryTokens.isEmpty() && !medicationTokens.isEmpty()) {
            int overlap = 0;
            for (String token : queryTokens) {
                if (medicationTokens.contains(token)) {
                    overlap++;
                }
            }
            score += ((double) overlap / (double) queryTokens.size()) * 1.4d;
        }

        if (hasCandidates) {
            score += 0.25d;
        }

        return score;
    }

    private double maxSimilarity(String query, String generic, String brand, String rxNorm, String searchable) {
        double genericSim = normalizedSimilarity(query, generic);
        double brandSim = brand.isBlank() ? 0d : normalizedSimilarity(query, brand);
        double rxNormSim = rxNorm.isBlank() ? 0d : normalizedSimilarity(query, rxNorm);
        double searchableSim = normalizedSimilarity(query, searchable);
        return Math.max(Math.max(genericSim, brandSim), Math.max(rxNormSim, searchableSim));
    }

    private double normalizedSimilarity(String query, String candidate) {
        if (query.isBlank() || candidate.isBlank()) {
            return 0d;
        }
        int distance = levenshteinDistance(query, candidate);
        int maxLen = Math.max(query.length(), candidate.length());
        return maxLen == 0 ? 0d : 1d - ((double) distance / (double) maxLen);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    private Map<String, Integer> embedding(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }

        String padded = "__" + value + "__";
        Map<String, Integer> vector = new HashMap<>();
        for (int i = 0; i <= padded.length() - 3; i++) {
            String gram = padded.substring(i, i + 3);
            vector.merge(gram, 1, Integer::sum);
        }
        return vector;
    }

    private double cosineSimilarity(Map<String, Integer> left, Map<String, Integer> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0d;
        }

        double dot = 0d;
        for (Map.Entry<String, Integer> entry : left.entrySet()) {
            dot += entry.getValue() * right.getOrDefault(entry.getKey(), 0);
        }

        double leftNorm = 0d;
        for (int value : left.values()) {
            leftNorm += value * value;
        }

        double rightNorm = 0d;
        for (int value : right.values()) {
            rightNorm += value * value;
        }

        if (leftNorm == 0d || rightNorm == 0d) {
            return 0d;
        }

        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private static String composeSearchableText(Medication medication) {
        String generic = genericName(medication);
        String brand = brandName(medication);
        String rxNorm = rxNormCode(medication);
        StringBuilder searchable = new StringBuilder(generic);
        if (!brand.isBlank()) {
            searchable.append(' ').append(brand);
        }
        if (!rxNorm.isBlank()) {
            searchable.append(' ').append(rxNorm);
        }
        return searchable.toString().trim();
    }

    private static String genericName(Medication medication) {
        return medication == null || medication.getGenericName() == null ? "" : medication.getGenericName().trim();
    }

    private static String brandName(Medication medication) {
        return medication == null || medication.getBrandName() == null ? "" : medication.getBrandName().trim();
    }

    private static String rxNormCode(Medication medication) {
        return medication == null || medication.getRxNormCode() == null ? "" : medication.getRxNormCode().trim();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = NORMALIZE_PATTERN.matcher(value.toLowerCase(Locale.ROOT)).replaceAll(" ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private static List<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String[] rawTokens = SPLIT_PATTERN.split(value.toLowerCase(Locale.ROOT));
        List<String> tokens = new ArrayList<>(rawTokens.length);
        for (String token : rawTokens) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private record ScoredMedication(Medication medication, double score) {
    }

    private static final class AhoCorasickAutomaton {
        private final Node root = new Node();

        AhoCorasickAutomaton(Set<String> patterns) {
            if (patterns == null || patterns.isEmpty()) {
                return;
            }
            for (String pattern : patterns) {
                insert(pattern);
            }
            buildFailureLinks();
        }

        Set<String> search(String text) {
            if (text == null || text.isBlank()) {
                return Set.of();
            }

            Set<String> matches = new LinkedHashSet<>();
            Node current = root;
            for (char character : text.toCharArray()) {
                while (current != root && !current.children.containsKey(character)) {
                    current = current.failure;
                }
                current = current.children.getOrDefault(character, root);
                if (!current.outputs.isEmpty()) {
                    matches.addAll(current.outputs);
                }
            }
            return matches;
        }

        private void insert(String pattern) {
            if (pattern == null || pattern.isBlank()) {
                return;
            }

            Node node = root;
            for (char character : pattern.toCharArray()) {
                node = node.children.computeIfAbsent(character, key -> new Node());
            }
            node.outputs.add(pattern);
        }

        private void buildFailureLinks() {
            ArrayDeque<Node> queue = new ArrayDeque<>();

            for (Node child : root.children.values()) {
                child.failure = root;
                queue.add(child);
            }

            while (!queue.isEmpty()) {
                Node current = queue.removeFirst();
                for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                    char transition = entry.getKey();
                    Node target = entry.getValue();

                    Node fallback = current.failure;
                    while (fallback != root && !fallback.children.containsKey(transition)) {
                        fallback = fallback.failure;
                    }

                    Node failureTarget = fallback.children.getOrDefault(transition, root);
                    target.failure = failureTarget;
                    target.outputs.addAll(failureTarget.outputs);
                    queue.add(target);
                }
            }
        }

        private static final class Node {
            private final Map<Character, Node> children = new HashMap<>();
            private Node failure;
            private final Set<String> outputs = new LinkedHashSet<>();

            private Node() {
                this.failure = this;
            }
        }
    }
}
