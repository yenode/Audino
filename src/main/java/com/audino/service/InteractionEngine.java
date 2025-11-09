package com.audino.service;

import com.audino.model.AlertLevel;
import com.audino.model.InteractionAlert;
import com.audino.model.Medication;
import com.audino.model.Patient;
import com.audino.model.Prescription;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class InteractionEngine {

    private final List<InteractionCheckStrategy> strategies;
    private final ExecutorService executorService;

    public InteractionEngine() {
        this.strategies = List.of(
            new AllergyCheckStrategy(),
            new DrugDrugCheckStrategy(),
            new ConditionCheckStrategy()
        );
        this.executorService = Executors.newFixedThreadPool(strategies.size());
    }

    public CompletableFuture<List<InteractionAlert>> checkAllInteractionsAsync(
        Patient patient, Prescription prescription, Map<String, Object> rules, List<Medication> allMedications) {

        List<CompletableFuture<List<InteractionAlert>>> futures = strategies.stream()
            .map(strategy -> CompletableFuture.supplyAsync(
                () -> strategy.check(patient, prescription, rules, allMedications), executorService))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .flatMap(future -> future.join().stream())
                .sorted(Comparator.comparing(InteractionAlert::getAlertLevel).reversed())
                .collect(Collectors.toList()));
    }

    public long getCriticalAlertCount(List<InteractionAlert> alerts) {
        return alerts.stream()
            .filter(alert -> alert.getAlertLevel() == AlertLevel.CRITICAL)
            .count();
    }

    public long getWarningAlertCount(List<InteractionAlert> alerts) {
        return alerts.stream()
            .filter(alert -> alert.getAlertLevel() == AlertLevel.WARNING)
            .count();
    }

    public long getInfoAlertCount(List<InteractionAlert> alerts) {
        return alerts.stream()
            .filter(alert -> alert.getAlertLevel() == AlertLevel.INFO)
            .count();
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}