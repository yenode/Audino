# Audino Class Diagram

## Purpose
This diagram models the concrete implementation structure across application, controller, service, model, and utility layers.

## PlantUML Source
The PlantUML source is now maintained in:

- `documentation/CLASS_DIAGRAM.puml`

## Modeling Notes
1. The service layer now explicitly models the strategy pattern through `InteractionCheckStrategy` and three concrete strategy classes.
2. The model layer now reflects medication polymorphism (`Medication` abstract base + three subclasses).
3. Relationships include actual ownership and associations: `Prescription` to `PrescribedDrug` and `InteractionAlert`, and `PrescribedDrug` to `Medication`.
4. Runtime configuration dependencies are represented through `ConfigurationManager` and `DataService`.
5. Legacy placeholders such as `JsonDataStore` and `RuleLoader` were removed because they are not implementation classes in this codebase.
