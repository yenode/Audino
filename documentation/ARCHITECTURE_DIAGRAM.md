# Audino Architecture Diagram Reference:

## Purpose:
The detailed architecture source for Audino is provided in ARCHITECTURE_DIAGRAM.puml. Layered application responsibilities and runtime data flow are represented.

## Coverage:
1. Presentation, service, strategy, domain, persistence, and search intelligence layers are represented.
2. SQLite integration and table level entities are represented.
3. Medication retrieval intelligence is represented with Aho Corasick token matching and NLP based similarity ranking.

## Render Command:
```bash
plantuml documentation/ARCHITECTURE_DIAGRAM.puml
```

## Notes:
Controller orchestration, interaction strategy execution, and transactional persistence flow are shown as directed dependencies.