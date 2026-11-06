# Query Validation Capability

## ADDED Requirements

### Requirement: Dual Validation Engine Support

The Query metamodel validation system SHALL support both EVL (Epsilon Validation Language) and Java-based validation engines running in parallel.

#### Scenario: EVL validation executes successfully
- **WHEN** a Query model is validated using EVL engine
- **THEN** all EVL constraints in `query.evl` and `query-plugin-validation.evl` are evaluated
- **AND** constraint violations are reported with constraint name and message

#### Scenario: Java validation executes successfully
- **WHEN** a Query model is validated using Java engine (Zeta framework)
- **THEN** all Java validation rules are evaluated
- **AND** constraint violations are reported with identical constraint names as EVL

#### Scenario: Both engines produce identical results
- **WHEN** the same Query model is validated by both EVL and Java engines
- **THEN** both engines report the same constraint violations (by constraint name)
- **AND** the severity levels (ERROR/WARNING) match between engines

### Requirement: Zeta Framework Integration

The Java validation implementation SHALL use the Judo Zeta validation framework for rule definition and execution.

#### Scenario: Zeta annotations define validation rules
- **WHEN** a validation rule class is created
- **THEN** it uses `@ValidationContext` to specify the target metamodel class
- **AND** it uses `@Constraint` for error-level rules
- **AND** it uses `@Critique` for warning-level rules
- **AND** it uses `@Guard` for conditional rule execution
- **AND** it uses `@Satisfies` for constraint dependencies

#### Scenario: QueryValidator entry point
- **WHEN** Java validation is invoked
- **THEN** `QueryValidator.validateQuery()` is called
- **AND** all registered validation rule classes are executed
- **AND** validation results are collected and returned

### Requirement: Validation Rule Constants

All validation rule identifiers SHALL be defined as constants in a central constants class.

#### Scenario: Constraint names use constants
- **WHEN** a validation rule is defined
- **THEN** its constraint name is referenced from `QueryValidationConstants`
- **AND** no hardcoded string literals are used for constraint names

#### Scenario: Guard method names use constants
- **WHEN** a guard method is referenced
- **THEN** its name is referenced from `QueryValidationConstants`
- **AND** no hardcoded string literals are used for guard method names

#### Scenario: Critique names use constants
- **WHEN** a critique (warning) rule is defined
- **THEN** its name is referenced from `QueryValidationConstants`
- **AND** no hardcoded string literals are used for critique names

### Requirement: Parameterized Validation Tests

Validation tests SHALL be parameterized to run against both EVL and Java validators.

#### Scenario: Test runs with EVL validator
- **WHEN** a validation test is executed with `ValidatorType.EVL`
- **THEN** the test model is validated using EVL
- **AND** expected constraint violations are verified

#### Scenario: Test runs with Java validator
- **WHEN** a validation test is executed with `ValidatorType.JAVA`
- **THEN** the test model is validated using Java/Zeta
- **AND** expected constraint violations are verified

#### Scenario: Same test case validates both engines
- **WHEN** a test method is annotated with `@ParameterizedTest` and `@EnumSource(ValidatorType.class)`
- **THEN** the test executes twice - once for EVL and once for Java
- **AND** both executions must pass for the test to succeed

### Requirement: Performance Validation

The validation system SHALL include performance tests comparing EVL and Java execution.

#### Scenario: Large model performance test
- **WHEN** a Query model with 10,000 elements is generated
- **AND** the model is validated by both EVL and Java engines
- **THEN** execution times for both engines are measured and logged
- **AND** the Java engine should demonstrate comparable or better performance

### Requirement: Validation Documentation

All implemented validation rules SHALL be documented.

#### Scenario: Rule documentation exists
- **WHEN** a validation rule is implemented
- **THEN** documentation in `docs/validation/query-validation-rules.md` describes the rule
- **AND** the documentation includes the constraint name, target class, and validation logic

#### Scenario: Framework documentation references Zeta
- **WHEN** a developer needs to understand the Java validation framework
- **THEN** `docs/validation/java-validation-framework.md` provides reference to Zeta documentation
- **AND** examples specific to Query metamodel are provided

### Requirement: Dependency Version Management

The Zeta framework dependency version SHALL be managed via a Maven property.

#### Scenario: Zeta version property defined
- **WHEN** the parent `pom.xml` is examined
- **THEN** a `judo-zeta-version` property is defined
- **AND** it is set to a SNAPSHOT version for development

#### Scenario: All Zeta dependencies use version property
- **WHEN** Zeta dependencies are declared in any `pom.xml`
- **THEN** the version uses `${judo-zeta-version}` property
- **AND** no hardcoded version strings are used for Zeta artifacts
