# Audino Architecture Diagram Reference:

## Purpose:
The detailed architecture source for Audino is provided in ARCHITECTURE_DIAGRAM.puml. Layered application responsibilities and runtime data flow are represented.

## Coverage:
1. Presentation, service, strategy, domain, persistence, and search intelligence layers are represented.
2. Embedded PostgreSQL integration natively tracking to `data/pg-data/` on port 5432 and utilizing the `HikariCP` connection pool is represented.
3. Medication retrieval intelligence is represented with Aho Corasick token matching and NLP based similarity ranking.
4. The `AppSeeder` workflow which hydrates the application with a Mock Clinical Dataset during deployments is included.

## Render Command:
```bash
plantuml documentation/ARCHITECTURE_DIAGRAM.puml
```

## Notes:
Controller orchestration, interaction strategy execution, search suggestions, and ACID-compliant transactional persistence flows are shown as directed dependencies.