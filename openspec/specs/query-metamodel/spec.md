# query-metamodel Specification

## Purpose

Defines the Query EMF Ecore metamodel (`query.ecore`) that represents expression-based transformation logic for constructing entity queries. The model captures selects, joins, features, filters, targets, and functions that are later used to generate RDBMS queries.

## Architecture

The metamodel is defined in `model/model/query.ecore` with namespace URI `http://blackbelt.hu/judo/meta/query`. The class hierarchy is:

- **NavigationBase** (abstract) — contains `joins : Join[*]` and `subSelects : SubSelect[*]`
- **FeatureHolder** (abstract) — contains `features : Feature[*]`
- **Node** (abstract, extends NavigationBase + FeatureHolder) — `alias : String`, `filters : Filter[*]`, `orderBys : OrderBy[*]`, `getType() : EClass`
  - **Select** — `from : EClass`, `targets : Target[*]`, `mainTarget : Target`, `singleColumnedSelect : boolean`
  - **SubSelect** — `transferRelation : EReference`, `select : Select`, `base : Node`, `embeddedSelect : Select`, `partner : Join`, `limit : Integer`, `offset : Integer`
  - **Filter** — `feature : Feature`
  - **OrderBy** — `descending : boolean`, `feature : Feature`
  - **Join** (abstract) — `partner : Node`, `getBase() : Node`
    - **ReferencedJoin** — `reference : EReference`
    - **SubSelectJoin** — `subSelect : SubSelect` (containment)
    - **ContainerJoin** — `references : EReference[*]`
    - **CastJoin** — `type : EClass`
    - **CustomJoin** — `type : EClass`, `transferRelation : EReference`, `navigationSql : String`, `sourceIdSetParameter : String`, `sourceIdParameter : String`
- **ParameterType** (abstract) — `functionParameters : FunctionParameter[*]`, `getNodes() : Node[]`
- **Feature** (abstract, extends ParameterType) — `targetMappings : FeatureTargetMapping[*]`, `filters : Filter[*]`, `orderBys : OrderBy[*]`, `aggregations : SubSelectFeature[*]`
  - **FeatureWithNode** — `node : Node`
    - **Attribute** — `sourceAttribute : EAttribute`
    - **IdAttribute**
    - **TypeAttribute**
  - **Constant** — `value : Object`
  - **Function** — `signature : FunctionSignature`, `parameters : FunctionParameter[*]`, `constraints : FunctionConstraint[*]`
  - **Variable** — `category : String`, `name : String`, `type : EDataType`
  - **SubSelectFeature** — `subSelect : SubSelect`, `feature : Feature`
  - **EntityTypeName** — `type : EClass`
- **Target** — `index : int`, `type : EClass`, `node : Node`, `referencedTargets : ReferencedTarget[*]`
- **FeatureTargetMapping** — `target : Target`, `targetAttribute : EAttribute`
- **ReferencedTarget** — `target : Target`, `reference : EReference`
- **FunctionParameter** — `parameterName : ParameterName`, `parameterValue : ParameterType`
- **FunctionConstraint** — `resultConstraint : ResultConstraint`, `value : String`

Enumerations: `FunctionSignature` (118+ operations), `ParameterName` (31 entries), `ResultConstraint` (PRECISION, SCALE, MAX_LENGTH).

## Requirements

### Requirement: Node provides entity type

Every concrete Node subclass SHALL return an EClass via `getType()` that represents the entity being queried.

#### Scenario: Select returns its from type
- **GIVEN** a Select with `from` set to an EClass
- **WHEN** `getType()` is called
- **THEN** the Select's `from` EClass is returned

#### Scenario: ReferencedJoin resolves type from reference
- **GIVEN** a ReferencedJoin with `reference` pointing to an EReference
- **WHEN** `getType()` is called
- **THEN** the EReference's target EClass is returned

### Requirement: Select contains targets and features

A Select SHALL contain at least one Target (lowerBound=1) and SHALL reference a `mainTarget`.

#### Scenario: Select with single target
- **GIVEN** a Select with one Target at index 0
- **WHEN** the model is validated
- **THEN** the Select is valid with `mainTarget` pointing to the single Target

### Requirement: Join navigates from partner

Every Join SHALL have a `partner` (lowerBound=1) and SHALL provide `getBase()` to recursively resolve the root Node.

#### Scenario: Chain of joins resolves to select
- **GIVEN** a ReferencedJoin whose partner is another ReferencedJoin whose partner is a Select
- **WHEN** `getBase()` is called on the outer join
- **THEN** the Select is returned

### Requirement: SubSelect supports relation navigation

A SubSelect SHALL reference a `select` (lowerBound=1) and MAY reference a `transferRelation` for navigation and a `base` Node as the starting point.

#### Scenario: SubSelect with pagination
- **GIVEN** a SubSelect with `limit=10` and `offset=20`
- **WHEN** the SubSelect is used in a query
- **THEN** the query limits results to 10 items starting at offset 20

### Requirement: Function supports typed parameters

A Function SHALL have a `signature` from `FunctionSignature` and MAY have typed `parameters` (FunctionParameter with `parameterName` and `parameterValue`).

#### Scenario: Binary comparison function
- **GIVEN** a Function with signature `EQUALS` and two parameters (LEFT, RIGHT)
- **WHEN** the function is evaluated
- **THEN** it compares the LEFT and RIGHT parameter values for equality

### Requirement: Feature maps to target attributes

A Feature SHALL support mapping to one or more Targets via `FeatureTargetMapping`, each specifying a `target` and optional `targetAttribute`.

#### Scenario: Attribute mapped to target column
- **GIVEN** an Attribute with a FeatureTargetMapping pointing to Target index 0 and a targetAttribute
- **WHEN** the query projection is built
- **THEN** the attribute value maps to the specified column of the target

### Requirement: Code generation produces complete model API

The MWE2 workflow (`model/src/workflow/generateModel.mwe2`) SHALL generate from `query.genmodel` into `model/src-gen/`:
- EMF model interfaces and implementations
- Fluent builders for all concrete classes
- Helper utilities
- `QueryModel` runtime loader with builder pattern
- `QueryModelResourceSupport` for ResourceSet creation

#### Scenario: Generated builders create valid model elements
- **WHEN** a builder (e.g., `SelectBuilder`) is used to create a model element
- **THEN** all required attributes and references are settable via the fluent API
