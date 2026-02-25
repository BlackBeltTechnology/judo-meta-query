# query-validation Specification

## Purpose

Validates Query models against Epsilon Validation Language (EVL) constraint rules to ensure structural and semantic correctness before downstream transformations consume them.

## Architecture

- **QueryEpsilonValidator** (`model/src/main/java/.../runtime/QueryEpsilonValidator.java`) — static utility that orchestrates validation by:
  1. Building an Epsilon `ExecutionContext` with the model wrapped as `"QUERY"` named resource
  2. Injecting `QueryUtils` as a helper context (`"queryUtils"`)
  3. Executing `query.evl` rules against the model
  4. Supporting expected errors/warnings for negative testing
- **EVL rule files** in `model/src/main/epsilon/validations/`:
  - `query.evl` — main model validation constraints (currently a TODO placeholder for JNG-4275)
  - `query-plugin-validation.evl` — Eclipse plugin-specific constraints
- **calculateQueryValidationScriptURI()** — resolves the EVL script location from the classpath, supporting JAR, bundle, and directory layouts

## Requirements

### Requirement: Validator runs EVL constraints on query models

`QueryEpsilonValidator.validateQuery(log, queryModel, scriptRoot)` SHALL execute all EVL rules from the `scriptRoot` URI against the provided `QueryModel` and throw `ScriptExecutionException` on unexpected errors.

#### Scenario: Valid model passes validation
- **GIVEN** a well-formed QueryModel and the default EVL script root
- **WHEN** `validateQuery` is called with no expected errors
- **THEN** no exception is thrown

#### Scenario: Model with violations throws exception
- **GIVEN** a QueryModel that violates an EVL constraint
- **WHEN** `validateQuery` is called with no expected errors
- **THEN** a `ScriptExecutionException` (or `EvlScriptExecutionException`) is thrown with details of unexpected errors

### Requirement: Validator supports expected error/warning lists

`validateQuery` SHALL accept `expectedErrors` and `expectedWarnings` collections. Validation SHALL succeed if all unexpected errors match expected errors, and no expected errors are missing.

#### Scenario: Expected errors are tolerated
- **GIVEN** a model that produces error "SomeConstraint: violation message"
- **WHEN** `validateQuery` is called with `expectedErrors` containing that message
- **THEN** no exception is thrown

### Requirement: Validator injects QueryUtils as helper

The Epsilon execution context SHALL inject a `QueryUtils` instance as `"queryUtils"` so that EVL rules can call utility methods from within constraint expressions.

#### Scenario: EVL rule uses queryUtils
- **GIVEN** an EVL rule that calls `queryUtils.getAllJoinsOfSelect(self)`
- **WHEN** validation runs on a Select element
- **THEN** the utility method is available and returns the join list

### Requirement: Script URI resolution supports multiple environments

`calculateQueryValidationScriptURI()` SHALL resolve the `validations/` directory from:
- A directory on the filesystem (development/test)
- A JAR file (`jar:file:...!/validations/`)
- An OSGi bundle (`bundle://...`)

#### Scenario: URI from JAR
- **GIVEN** the code source location ends with `.jar`
- **WHEN** `calculateQueryValidationScriptURI()` is called
- **THEN** the returned URI is `jar:<jar-path>!/validations/`

#### Scenario: URI from filesystem
- **GIVEN** the code source location is a directory
- **WHEN** `calculateQueryValidationScriptURI()` is called
- **THEN** the returned URI is `<directory>/validations/`

### Requirement: Validator supports caching

`validateQuery` SHALL accept an optional `useCache` parameter to enable Epsilon model caching for improved performance on repeated validations.

#### Scenario: Validation with cache enabled
- **GIVEN** a QueryModel
- **WHEN** `validateQuery` is called with `useCache=true`
- **THEN** the wrapped EMF model context uses caching
