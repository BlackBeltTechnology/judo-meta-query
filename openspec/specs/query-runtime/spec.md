# query-runtime Specification

## Purpose

Provides hand-written runtime utilities for working with Query models — loading/saving models, formatting query trees for debugging, generating aliases, and creating EMF ResourceSets. These utilities complement the generated code in `model/src-gen/`.

## Architecture

Hand-written classes in `model/src/main/java/hu/blackbelt/judo/meta/query/runtime/`:

- **QueryUtils** — static utilities for join alias formatting, recursive join collection, and select tree pretty-printing
- **StringUtils** — string padding helper (`leftPad`)

Generated classes (in `model/src-gen/`) that form the runtime API:

- **QueryModel** — fluent builder for loading/building models; `buildQueryModel()` creates new models, `loadQueryModel(LoadArguments)` loads from streams/URIs; inner `LoadArguments` with builder
- **QueryModelResourceSupport** — creates EMF ResourceSets configured with the Query metamodel package; `queryModelResourceSupportBuilder()` fluent builder, `createQueryResourceSet()` static factory

## Requirements

### Requirement: QueryModel supports building new models

`QueryModel.buildQueryModel()` SHALL return a builder that accepts a `QueryModelResourceSupport`, a `name`, and produces a valid `QueryModel` instance.

#### Scenario: Build empty query model
- **GIVEN** a QueryModelResourceSupport with a URI
- **WHEN** `QueryModel.buildQueryModel().queryModelResourceSupport(support).name("test").build()` is called
- **THEN** a QueryModel is returned with an accessible ResourceSet and Resource

### Requirement: QueryModel supports loading from file

`QueryModel.loadQueryModel(LoadArguments)` SHALL load a `.model` file into an EMF ResourceSet and return a QueryModel instance.

#### Scenario: Load model from file URI
- **GIVEN** a `.model` file at a known path and a ResourceSet from `QueryModelResourceSupport.createQueryResourceSet()`
- **WHEN** `QueryModel.loadQueryModel(queryLoadArgumentsBuilder().resourceSet(rs).uri(fileUri).name("test").build())` is called
- **THEN** the model is loaded and all EObjects are accessible via `getResourceSet()`

### Requirement: QueryModel supports loading from InputStream

`QueryModel.loadQueryModel(LoadArguments)` SHALL accept an `inputStream` parameter for loading models from bundle resources.

#### Scenario: Load model from bundle stream
- **GIVEN** an InputStream from a bundle entry
- **WHEN** `loadQueryModel` is called with the stream and a name
- **THEN** the model is loaded and registered with the provided name

### Requirement: QueryUtils collects all joins recursively

`QueryUtils.getAllJoinsOfSelect(Select)` SHALL return a flat list of all Joins reachable from a Select, including nested joins.

#### Scenario: Select with nested joins
- **GIVEN** a Select with Join A, which has child Join B, which has child Join C
- **WHEN** `getAllJoinsOfSelect(select)` is called
- **THEN** the result contains [A, B, C] in traversal order

### Requirement: QueryUtils formats join aliases

`QueryUtils.getNextJoinAlias(AtomicInteger)` SHALL produce aliases in the format `j01`, `j02`, etc. `getNextSubSelectAlias(AtomicInteger)` SHALL produce `ss01`, `ss02`, etc.

#### Scenario: Sequential alias generation
- **GIVEN** an AtomicInteger initialized to 0
- **WHEN** `getNextJoinAlias` is called twice
- **THEN** the results are `"j01"` and `"j02"`

### Requirement: QueryUtils formats select trees

`QueryUtils.formatSelect(Select)` SHALL produce a human-readable indented text representation of the query tree showing features, from entity, joins, targets, filters, order-by, and subselects.

#### Scenario: Format a simple select
- **GIVEN** a Select with from=Person, features=[name], and one Target
- **WHEN** `formatSelect(select)` is called
- **THEN** a multi-line string is returned showing SELECT, FEATURES, FROM, JOINING, and TO sections

### Requirement: QueryModelResourceSupport creates configured ResourceSets

`QueryModelResourceSupport.queryModelResourceSupportBuilder().uri(uri).build()` SHALL create a ResourceSet with the Query EPackage registered and a Resource at the given URI.

#### Scenario: Build resource support with URI
- **GIVEN** a URI string `"urn:query.judo-meta-query"`
- **WHEN** the builder is used to create a QueryModelResourceSupport
- **THEN** the support's ResourceSet has the Query package registered and a Resource at the URI
