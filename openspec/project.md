# Project Context

## Purpose

**Judo Query Meta** is an Eclipse/Tycho-based metamodel project that:
- Defines a Query metamodel via EMF/Ecore for Expression-based query transformation
- Generates Java code from the model using MWE2 workflows
- Provides both Eclipse plugin and OSGi standalone runtime
- Distributes via both Maven Central and Eclipse P2 repositories

This is not an SQL model, but a model representing transformation logic for Expression-based queries. The output is used later for RDBMS query generation.

## Tech Stack

### Core Technologies
- **Java 21** - Primary language
- **Eclipse Modeling Framework (EMF)** 2.38.0+ - Metamodel foundation
- **Ecore** - Model definition language
- **MWE2** (Model Workflow Engine) 2.13.0 - Code generation workflows
- **Epsilon** 2.8.0 - Model validation (EVL) and object language (EOL)
- **Tycho** 4.0.13 - Eclipse plugin build

### Runtime
- **Apache Karaf** 4.4.7 - OSGi container
- **Apache Felix** 6.0.0 - OSGi bundle plugin

### Build & Testing
- **Maven** 3.9.4+ with wrapper
- **JUnit 5** - Unit testing
- **Pax Exam** 4.13.5 - OSGi integration testing

## Project Conventions

### Code Style
- Java 21 language features (records, pattern matching, sealed classes where applicable)
- Use Lombok for boilerplate reduction (`@Getter`, `@Setter`, `@Builder`, `@Slf4j`)
- EMF-generated code follows GenModel conventions
- Immutable objects preferred for validation results and cache keys
- Functional interfaces for validation rules and guards

### Architecture Patterns
- **EMF/Ecore patterns** for metamodel definition and manipulation
- **Annotation-based configuration** for validation rules
- **Functional interfaces** for validation logic (lambdas supported)
- **Registry pattern** for scanning and discovering validators
- **Phased execution** for dependency resolution (satisfies)
- **Caching** for expensive operations (extension methods, satisfies results)

### Testing Strategy
- Unit tests in `model-test/` module using JUnit 5
- Parameterized tests to run both EVL and Java validation
- Expected errors/warnings passed to validator for assertion
- Model fixtures created using EMF builders
- OSGi integration tests via Pax Exam in `osgi-itest/`

### Git Workflow
- **Main Branch:** `develop`
- **Versioning:** SNAPSHOT-based development (currently 1.0.3-SNAPSHOT)
- Feature branches for significant changes
- OpenSpec proposals for architectural changes

## Domain Context

### Query Metamodel Classes
| Class | Purpose |
|-------|---------|
| `Node` | Abstract base with `getType()` returning EClass representing an entity |
| `Select` | Actual "select" for an entity; `from` is the EClass entity to be selected |
| `SubSelect` | Runtime query representation; navigates on relations |
| `SubSelectFeature` | Subselects used in "where" part |
| `Target` | Projection - describes how to represent query results |
| `Join` | `getBase()` returns what it is joined to (recursively queries partners) |
| `Feature` | Abstract - attribute, calculated value, constants etc. that select/join returns |
| `FeatureWithNode` | Features with strong relation to entities |
| `Attribute` | Entity attribute |
| `IdAttribute` | Entity ID |
| `TypeAttribute` | Entity type, version, create time etc. |
| `EntityTypeName` | Used with inheritance |
| `Variable` | Category, name + type |
| `Function` | Operations with signature (AND, OR, EQUALS, COUNT, SUM, etc.) |
| `Constant` | Constant values in queries |
| `Filter` | Filter expressions |
| `OrderBy` | Ordering specification |

### Validation System
Current validation uses **Epsilon Validation Language (EVL)**:
- Rules in `model/src/main/epsilon/validations/`
- Entry point: `query-plugin-validation.evl`
- Two severity levels: Constraint (ERROR) and Critique (WARNING)
- Currently minimal (mostly placeholder with TODO: JNG-4275)

### Key Validation Concepts
- **Constraint**: Error-level rule that must pass
- **Critique**: Warning-level rule (advisory)
- **Guard**: Condition that determines if rule should evaluate
- **Satisfies**: Dependency on another constraint's result (cached)
- **Context**: EClass type the rule applies to

## Important Constraints

### Build Constraints
- Tycho build requires Eclipse plugin structure
- OSGi bundle manifests must be maintained
- P2 update site structure for Eclipse distribution

### Validation Constraints
- EVL rules must remain functional during Java framework migration
- Test parity: Java tests must mirror EVL tests exactly
- Error message format must match EVL format
- Cache results for `satisfies()` calls to avoid re-evaluation

## External Dependencies

### Eclipse Platform
- EMF Runtime 2.38.0+
- Xtext/Xtend (for DSL tooling)

### Epsilon Runtime
- EVL (Epsilon Validation Language) for model validation
- EOL (Epsilon Object Language) for helper operations
- EMC (Epsilon Model Connectivity) for EMF integration

### Judo Zeta Validation Framework
- **hu.blackbelt.judo.zeta:hu.blackbelt.judo.zeta.validation-core** - Reusable validation framework
- **hu.blackbelt.judo.zeta:hu.blackbelt.judo.zeta.zeta-annotations** - Validation annotations
- **hu.blackbelt.judo.zeta:hu.blackbelt.judo.zeta.zeta-common** - Common utilities

### Build Infrastructure
- Maven Central for artifact publication
- Eclipse P2 for plugin distribution
- GitHub Actions CI/CD pipeline

## Module Overview

| Module | Purpose |
|--------|---------|
| `model/` | Core Query metamodel, EMF code, Epsilon validation, Java validation |
| `model-test/` | Unit tests for metamodel and validation |
| `osgi/` | OSGi bundle repackaging |
| `osgi-itest/` | OSGi integration tests |
| `feature/` | Eclipse feature packaging |
| `site/` | P2 update site |

## Active Changes

See `openspec/changes/` for in-progress proposals.
