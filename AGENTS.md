# judo-meta-query - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-meta-query
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21 (JavaSE-21)
**Build System:** Maven 3.9.4+ with Tycho 4.0.13 (Eclipse integration)

1. Defines the **Query metamodel** — an EMF Ecore model representing expression-based transformation logic for generating RDBMS queries
2. Provides **runtime utilities** for model loading, validation (via Epsilon EVL), and query formatting
3. Distributes the metamodel as an **Eclipse plugin** (with feature and P2 update site), a **standalone Maven artifact**, and an **OSGi bundle**
4. Uses **MWE2 code generation** to produce model classes, fluent builders, and helper utilities from the Ecore definition
5. Part of the [judo-community](https://github.com/BlackBeltTechnology/judo-community) ecosystem

## Directory Structure

```
judo-meta-query/
├── model/                  # Core Eclipse plugin — Ecore model + generated code + runtime
│   ├── model/              # query.ecore and query.genmodel definitions
│   ├── src/main/java/      # Hand-written runtime utilities
│   ├── src/main/epsilon/   # EVL validation rules
│   ├── src/workflow/        # MWE2 code generation workflow
│   ├── src-gen/            # Generated EMF model code (DO NOT EDIT)
│   └── META-INF/           # OSGi MANIFEST.MF
├── model-test/             # JUnit 5 tests for model validation and loading
├── osgi/                   # OSGi bundle repackaging
├── osgi-itest/             # Pax Exam + Karaf integration tests
├── feature/                # Eclipse feature definition
├── site/                   # Eclipse P2 update site
├── .github/workflows/      # CI/CD GitHub Actions
└── openspec/               # OpenSpec configuration
```

## Core Modules

### Model Layer

| Module | Type | Purpose |
|--------|------|---------|
| `model/` | eclipse-plugin | Ecore metamodel (`query.ecore`), GenModel, MWE2 workflow, generated EMF classes (`src-gen/`), hand-written runtime code, and Epsilon validation rules |

### Testing

| Module | Type | Purpose |
|--------|------|---------|
| `model-test/` | jar | JUnit 5 tests — `QueryValidationTest`, `QueryExecutionContextTest`, `QueryModelLoaderTest` |
| `osgi-itest/` | jar | OSGi integration tests using Pax Exam 4.13.5 with Karaf 4.4.7 container |

### Distribution

| Module | Type | Purpose |
|--------|------|---------|
| `osgi/` | bundle (maven-bundle-plugin) | Repackages the model as an OSGi bundle with service metadata for non-Eclipse consumers |
| `feature/` | eclipse-feature | Eclipse feature definition for plugin installation |
| `site/` | eclipse-repository | Eclipse P2 update site for distribution |

## Technology Stack

### Core Technologies
- **Eclipse EMF (Ecore)** — metamodel framework; `org.eclipse.emf.ecore` 2.38.0, `org.eclipse.emf.common` 2.41.0
- **Epsilon Runtime** — model validation via EVL (Epsilon Validation Language); version 2.8.0
- **Xtext 2.39.0 / MWE2** — code generation workflows
- **OSGi** — `osgi.core` 7.0.0, `osgi.cmpn` 7.0.0

### Build & Quality
- **Maven 3.9.4+** with Maven wrapper (`./mvnw`)
- **Tycho 4.0.13** — Eclipse plugin building, version management, P2 metadata
- **JUnit Jupiter 5.9.1** — unit testing
- **Pax Exam 4.13.5** — OSGi integration testing with Karaf 4.4.7
- **JaCoCo 0.8.12** — code coverage
- **SLF4J 2.0.16 + Logback 1.5.12** — logging
- **Lombok 1.18.34** — used in non-Eclipse modules only (Tycho incompatibility)
- **Flatten Maven Plugin 1.3.0** — CI-friendly POM resolution

## Build Commands

```bash
# Full build (all modules)
./mvnw clean install

# Run all tests
./mvnw clean test

# Run a single test class
./mvnw test -pl model-test -Dtest=QueryValidationTest

# Skip tests
./mvnw clean install -DskipTests

# Code coverage
./mvnw clean test jacoco:report

# Update Eclipse site category versions
mvn clean install -P update-category-versions -f site/pom.xml
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Active by default — includes all submodules. Deactivate with `-DskipModules=true` |
| `sign-artifacts` | Signs artifacts using `sign-maven-plugin` (CI releases) |
| `release-dummy` | Deploys to local `/tmp/` directory for testing |
| `release-judong` | Deploys to Judong Nexus snapshot repository |
| `release-central` | Deploys to Maven Central via Sonatype OSSRH |
| `generate-github-asciidoc-diagrams` | Generates PNG diagrams from PlantUML in AsciiDoc |
| `update-source-code-license` | Updates EPL-2.0 license headers in source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `model/model/query.ecore` | Ecore metamodel definition — the source of truth for the Query model |
| `model/model/query.genmodel` | EMF generator model — configures code generation parameters |
| `model/src/workflow/generateModel.mwe2` | MWE2 workflow — orchestrates code generation pipeline |
| `model/META-INF/MANIFEST.MF` | OSGi bundle manifest for the Eclipse plugin |
| `model/plugin.xml` | Eclipse plugin extensions (EVL validation, EMF parsers, utilities) |
| `model/src/main/epsilon/validations/query.evl` | Main Epsilon validation rules |
| `model/src/main/epsilon/validations/query-plugin-validation.evl` | Eclipse plugin-specific validation rules |
| `logback-test.xml` | Shared test logging configuration (SLF4J/Logback) |
| `pom.xml` | Root Maven POM — version management, dependency BOMs, plugin configuration |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+ (or use `./mvnw`)

**Eclipse IDE (optional):**
- m2e, Epsilon, Modeling Tools plugins
- For code generation: Xtext, MWE, MWE2 plugins
- Install the metamodel via P2 update site for editor support

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** `revision` property (currently `1.0.3-SNAPSHOT`); Tycho bridges Maven SNAPSHOT ↔ Eclipse `.qualifier`
- **Branching:** GitFlow — `feature/JNG-*`, `release/*`, `bugfix/JNG-*`, `support/JNG-*`, `hotfix/JNG-*`
- **CI/CD:** GitHub Actions — `build.yml` (main pipeline), `release.yml` (manual releases), `merge-pr-tagged.yml` (auto-merge)
- **Rule:** Every commit must reference a JIRA ticket (`JNG-xxx`)

## Important Notes

1. **Never edit `model/src-gen/`** — all code there is generated from `query.ecore` via the MWE2 workflow. Regenerate after model changes.
2. **No Lombok in Eclipse plugin modules** — Tycho does not support Lombok annotation processing. Only use Lombok in `model-test/`, `osgi/`, and `osgi-itest/`.
3. **Hand-written runtime code** lives in `model/src/main/java/hu/blackbelt/judo/meta/query/runtime/`:
   - `QueryUtils` — join alias formatting, recursive join collection, select tree formatting
   - `QueryEpsilonValidator` — programmatic EVL validation runner
   - `StringUtils` — string padding helper
4. **Generated code** includes `QueryModel` (model loader with fluent builder API) and `QueryModelResourceSupport` (EMF ResourceSet factory) in the `runtime` and `support` packages respectively.
5. **Validation rules** are currently minimal (`query.evl` contains only a TODO comment for JNG-4275). The `query-plugin-validation.evl` provides Eclipse-specific constraints.
6. **Version updates** must be applied through the `revision` property in the root `pom.xml`. Do not change individual module versions directly.

## Related Documentation

- [README.md](README.md) — project overview with metamodel class diagram
- [CONTRIBUTING.md](CONTRIBUTING.md) — development setup, build lifecycle, code generation details
- [.github/CIFLOW.md](.github/CIFLOW.md) — branching strategy and CI/CD workflow documentation
- [judo-community](https://github.com/BlackBeltTechnology/judo-community) — parent ecosystem documentation
