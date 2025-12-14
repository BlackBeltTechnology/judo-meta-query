<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# Judo Query Meta - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-meta-query
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4+ with Tycho (Eclipse build tooling)

This is an Eclipse/Tycho-based metamodel project that:
1. **Defines** a Query metamodel via EMF/Ecore for Expression-based query transformation
2. **Generates** Java code from the model using MWE2 workflows
3. **Provides** both Eclipse plugin and OSGi standalone runtime
4. **Distributes** via both Maven Central and Eclipse P2 repositories

This is not an SQL model, but a model representing transformation logic for Expression-based queries. The output is used later for RDBMS query generation.

## Directory Structure

```
judo-meta-query/
├── model/                          # Core Query metamodel (Ecore)
├── model-test/                     # Unit tests for metamodel
├── osgi/                           # OSGi bundle repackaging
├── osgi-itest/                     # OSGi integration tests (Pax Exam)
├── feature/                        # Eclipse feature (model)
├── site/                           # Eclipse P2 update site
└── openspec/                       # OpenSpec change management (if present)
```

## Core Modules

### Model Definition Layer

| Module | Type | Purpose |
|--------|------|---------|
| `model/` | eclipse-plugin | Core Query metamodel via Ecore (`query.ecore`). Generates EMF code, builders, helpers. Contains Epsilon validation rules. |
| `model-test/` | test | Unit tests for Query metamodel using JUnit 5 and Epsilon runtime |

### Runtime/OSGi Layer

| Module | Type | Purpose |
|--------|------|---------|
| `osgi/` | bundle | Repackages model for OSGi environments using Apache Felix Bundle Plugin |
| `osgi-itest/` | test | Pax Exam integration tests for Karaf container (4.4.7) |

### Distribution Layer

| Module | Type | Purpose |
|--------|------|---------|
| `feature/` | eclipse-feature | Bundles model and plugins |
| `site/` | eclipse-repository | P2 update site for Eclipse distribution |

## Query Metamodel Structure

The core metamodel (`model/model/query.ecore`) defines these key concepts:

| Class | Purpose |
|-------|---------|
| `Node` | Abstract base with `getType()` returning EClass representing an entity |
| `Select` | Actual "select" for an entity; `from` is the EClass entity to be selected |
| `SubSelect` | Runtime query representation; navigates on relations |
| `SubSelectFeature` | Subselects used in "where" part |
| `Target` | Projection - describes how to represent query results |
| `Join` | `getBase()` returns what it is joined to (recursively queries partners) |
| `Feature` | Abstract - attribute, calculated value, constants etc. that select/join returns |
| `FeatureWithNode` | Features with strong relation to entities (Attribute, IdAttribute, TypeAttribute) |
| `Attribute` | Entity attribute |
| `IdAttribute` | Entity ID |
| `TypeAttribute` | Entity type, version, create time etc. |
| `EntityTypeName` | Used with inheritance |
| `Variable` | Category, name + type (e.g., `String!getVariable(...)`) |
| `Function` | Operations with signature (AND, OR, EQUALS, COUNT, SUM, etc.) |
| `Constant` | Constant values in queries |
| `Filter` | Filter expressions |
| `OrderBy` | Ordering specification |

**Validation Rules:**
- **EVL (Epsilon):** Located in `model/src/main/epsilon/validations/`
  - `query.evl` - Core query validation rules
  - `query-plugin-validation.evl` - Plugin-specific validations

## Technology Stack

### Core Technologies
- **Eclipse Modeling Framework (EMF)** 2.38.0+ - Metamodel foundation
- **Ecore** - Model definition language
- **MWE2** (Model Workflow Engine) 2.13.0 - Code generation workflows
- **Epsilon** 2.8.0 - Model validation and transformation
- **Tycho** 4.0.13 - Eclipse plugin build

### Runtime
- **Apache Karaf** 4.4.7 - OSGi container
- **Apache Felix** 6.0.0 - OSGi bundle plugin
- **Pax Exam** 4.13.5 - OSGi testing

### Build & Quality
- **Maven** 3.9.4+ with wrapper
- **JaCoCo** 0.8.12 - Code coverage
- **SonarQube** 3.9.1 - Code quality
- **Lombok** 1.18.34 - Annotation processing

## Build Commands

```bash
# Standard build
mvn clean install
# or with wrapper
./mvnw clean install

# Memory requirements (configured in .mvn/jvm.config)
# -Xms1024m -Xmx2048m
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Includes all 6 submodules (default) |
| `sign-artifacts` | GPG signing for release |
| `release-central` | Maven Central deployment |
| `release-judong` | Internal Judo repository |

## Code Generation Flow

1. **MWE2 Workflow** (`model/src/workflow/generateModel.mwe2`)
   - Generates EMF code from `query.ecore`
   - Produces GenModel-based Java classes
   - Generates builders and helpers

2. **Model Compilation**
   - Tycho compiles eclipse-plugin modules
   - OSGi bundle compilation with Felix

3. **Feature/Site Building**
   - P2 metadata generation
   - Feature packaging
   - Update site assembly

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM with module definitions and plugin management |
| `.mvn/jvm.config` | JVM arguments for Maven build |
| `.mvn/extensions.xml` | Maven extensions |
| `model/model/query.ecore` | Core metamodel definition |
| `model/model/query.genmodel` | EMF code generation model |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+
- Eclipse IDE with:
  - m2e (Maven integration)
  - Epsilon plugin
  - Modeling tools
  - Xtext/Xtend plugins

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** SNAPSHOT-based development (currently 1.0.3-SNAPSHOT)
- **Version Placeholder:** `$VERSION_PLACEHOLDER$` in model metadata
- **Release Process:** CI/CD via GitHub Actions with Maven Central and P2 deployment

## Important Notes

1. **Understand EMF/Ecore patterns** before modifying model code
2. **Respect Tycho build constraints** when modifying Eclipse plugins
3. **Validation rules** are in EVL (Epsilon) format in `model/src/main/epsilon/validations/`
4. **Use OpenSpec for significant changes** - See `openspec/AGENTS.md` for proposal workflow (if present)

## Related Documentation

- `README.md` - Project overview
- `AGENTS.md` - Detailed project documentation for AI assistants
- `openspec/AGENTS.md` - OpenSpec workflow for spec-driven development (if present)
