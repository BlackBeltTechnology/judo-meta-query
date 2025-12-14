# Change: Add Zeta Java Validation Framework

## Why

The current Query metamodel validation uses Epsilon Validation Language (EVL) which has limitations:
- No IDE support for debugging
- Interpretation overhead at runtime
- Limited test tooling integration
- Difficult to maintain and extend

The Judo Zeta validation framework provides a modern, annotation-based Java alternative that:
- Offers full IDE integration (debugging, refactoring, autocomplete)
- Compiles to native Java for better performance
- Supports parallel validation for large models
- Provides better caching and dependency resolution

This change implements dual validation (EVL + Java running in parallel) following the pattern established in `judo-meta-esm`.

## What Changes

### Model Module (`model/`)
- **ADDED** Java validation framework integration using judo-zeta
- **ADDED** `QueryValidator.java` - Main validator entry point
- **ADDED** Validation rule classes organized by metamodel category
- **ADDED** Extension method classes for reusable helpers
- **ADDED** Constant definitions for constraint names, guard methods, etc.
- **MODIFIED** `pom.xml` - Add judo-zeta dependencies with version property

### Model Test Module (`model-test/`)
- **MODIFIED** Existing validation tests to use parameterized tests
- **ADDED** `ValidatorType` enum (EVL, JAVA)
- **ADDED** `AbstractQueryValidationTest` base class for dual validation
- **ADDED** Performance test comparing EVL vs Java validation (10,000 elements)

### Documentation (`docs/`)
- **ADDED** `docs/validation/` directory with validation documentation
- **ADDED** Reference to Judo Zeta documentation (without copying content)
- **MODIFIED** Convert any `.adoc` files (except `pages/`) to Markdown
- **MODIFIED** Convert PlantUML diagrams to Mermaid

### Build Configuration
- **MODIFIED** Parent `pom.xml` - Add `judo-zeta-version` property (SNAPSHOT)
- **MODIFIED** All pom.xml files to use `${judo-zeta-version}` property

## Impact

- **Affected specs:** query-validation (new capability)
- **Affected code:**
  - `model/src/main/java/hu/blackbelt/judo/meta/query/validation/` (new)
  - `model/pom.xml`
  - `model-test/src/test/java/hu/blackbelt/judo/meta/query/runtime/` (modified)
  - `model-test/pom.xml`
  - `pom.xml` (parent)
  - `docs/` (new/modified)
- **Dependencies:** judo-zeta-validation-core, judo-zeta-annotations, judo-zeta-common
- **Breaking changes:** None - EVL validation continues to work alongside Java validation
