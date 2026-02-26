# osgi-bundle Specification

## Purpose

Repackages the Query metamodel as an OSGi bundle and provides automatic model discovery and registration via bundle tracking. This allows non-Eclipse OSGi containers (e.g., Apache Karaf) to consume Query models from installed bundles.

## Architecture

- **osgi module** — Maven bundle packaging using `maven-bundle-plugin`. Exports all `hu.blackbelt.judo.meta.query.*` packages and embeds the model JAR.
- **QueryModelBundleTracker** (`osgi/src/main/java/.../osgi/QueryModelBundleTracker.java`) — OSGi Declarative Services component that:
  - References `BundleTrackerManager` from `hu.blackbelt.osgi.utils`
  - Tracks bundles with `Query-Models` manifest header
  - Loads models from bundle entries and registers them as OSGi services
  - Unregisters models when bundles are stopped
- **osgi-itest module** — Integration tests using Pax Exam 4.13.5 with Karaf 4.4.7

## Requirements

### Requirement: Bundle tracker discovers models from manifest header

`QueryModelBundleTracker` SHALL track bundles that have a `Query-Models` header in their MANIFEST.MF and load the referenced model files.

#### Scenario: Bundle with Query-Models header is installed
- **GIVEN** a bundle with MANIFEST header `Query-Models: name=myQuery;file=model/query.model`
- **WHEN** the bundle is started in the OSGi container
- **THEN** the `QueryRegisterCallback` loads the model from `model/query.model` and registers a `QueryModel` OSGi service

### Requirement: Models are registered as OSGi services

Loaded QueryModels SHALL be registered as OSGi services of type `QueryModel` with service properties derived from `queryModel.toDictionary()`.

#### Scenario: Service registration
- **GIVEN** a successfully loaded QueryModel with name "myQuery"
- **WHEN** the model is registered
- **THEN** `bundleContext.registerService(QueryModel.class, queryModel, queryModel.toDictionary())` is called
- **AND** the service is discoverable via standard OSGi service lookup

### Requirement: Duplicate model names are rejected

If a model with the same `name` is already registered, the tracker SHALL log an error and skip registration.

#### Scenario: Duplicate model name
- **GIVEN** a QueryModel "myQuery" is already registered
- **WHEN** another bundle provides a model with name "myQuery"
- **THEN** an error is logged: "Query model already loaded: myQuery"
- **AND** the duplicate is not registered

### Requirement: Models are unregistered on bundle stop

When a tracked bundle is stopped, the tracker SHALL unregister all QueryModel services that were loaded from that bundle.

#### Scenario: Bundle uninstall triggers cleanup
- **GIVEN** a running bundle with a registered QueryModel "myQuery"
- **WHEN** the bundle is stopped
- **THEN** `modelServiceRegistration.unregister()` is called
- **AND** the model is removed from internal maps

### Requirement: OSGi integration tests validate container deployment

The `osgi-itest` module SHALL verify that the Query model bundle loads correctly in a Karaf container using Pax Exam.

#### Scenario: Bundle loads in Karaf
- **GIVEN** the osgi bundle is provisioned into a Karaf 4.4.7 test container
- **WHEN** the container starts
- **THEN** the bundle reaches ACTIVE state and the Query EPackage is registered
