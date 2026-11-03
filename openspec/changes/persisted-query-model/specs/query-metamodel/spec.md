## ADDED Requirements

### Requirement: Query model carries a persisted index root
The metamodel SHALL define the class `QueryModelInfo` with the attributes `formatVersion`, `modelName`,
`generator`, `asmDigest`, `measureDigest`, `expressionDigest`, `nextSourceIndex` and `nextTargetIndex`. One query
resource SHALL hold at most one `QueryModelInfo` root.

#### Scenario: Stamps and counters are stored
- **GIVEN** a `QueryModelInfo` with a format version, model name, generator, three digests and both counters set
- **WHEN** the query resource is saved and loaded again
- **THEN** every attribute of the loaded `QueryModelInfo` equals the saved value

#### Scenario: Builder exists for the new root
- **WHEN** the code is generated from `query.genmodel`
- **THEN** a fluent builder for `QueryModelInfo` exists next to the other generated builders

### Requirement: Query model index entry types
The metamodel SHALL define `QueryEntry` (`type : EClass`, `select : Select`), `NavigationEntry`
(`reference : EReference`, `subSelect : SubSelect`), `DataQueryEntry` (`attribute : EAttribute`,
`subSelect : SubSelect`) and `EntityTypeExpressionsEntry` (`entityType : EClass`, getter-attribute entries and
getter-reference entries), all contained in `QueryModelInfo`.

#### Scenario: Entries are contained by the index root
- **GIVEN** a `QueryModelInfo` with a query entry, a navigation entry, a data query entry and an entity type
  expressions entry
- **WHEN** the model is traversed
- **THEN** every entry is reached as content of the `QueryModelInfo`

#### Scenario: Getter entries pair a feature with an expression
- **GIVEN** an `EntityTypeExpressionsEntry` for an entity type
- **WHEN** a getter-attribute entry and a getter-reference entry are added
- **THEN** each pairs an `EAttribute` or an `EReference` with one expression object

### Requirement: Index references are non-containment
Every reference from `QueryModelInfo` and its entries to a `Select`, a `SubSelect`, an ASM element
(`EClass`, `EReference`, `EAttribute`) or an expression object SHALL be a non-containment reference. Query
`Select` roots SHALL remain roots of the query resource.

#### Scenario: Query roots stay roots
- **GIVEN** a `QueryEntry` whose `select` is a `Select` that is a root of the query resource
- **WHEN** the resource is saved and loaded again
- **THEN** the `Select` is still a root of the resource and is referenced, not contained, by the entry

#### Scenario: ASM targets are referenced
- **WHEN** a navigation entry references an `EReference` of the ASM model
- **THEN** saving the query resource writes a cross-resource reference and does not copy the `EReference`

### Requirement: Expression references are typed EObject
The expression references of the getter-attribute and getter-reference entries SHALL be typed `EObject`, so the
Query metamodel has no dependency on the Expression metamodel. The ordered transfer relations, static entity
attributes and static entity references SHALL be non-containment lists of `EReference` and `EAttribute`.

#### Scenario: No expression metamodel dependency
- **WHEN** `judo-meta-query` is built
- **THEN** its declared dependencies are unchanged and do not include `judo-meta-expression`

#### Scenario: Object of another resource is referenced
- **GIVEN** an `EObject` held in a second resource
- **WHEN** a getter entry references it and both resources are saved and loaded together
- **THEN** the loaded reference resolves to the object of the second resource

### Requirement: Query model with index root is valid
`QueryModelResourceSupport.isValid()` SHALL return true for a query resource that contains a `QueryModelInfo`
root, query roots and cross-resource references to ASM and expression objects, and the store's query validators
(`query-validation`) SHALL accept such a resource.

#### Scenario: Valid with the index root
- **GIVEN** a query resource with query roots and a `QueryModelInfo` referencing ASM objects and `EObject`s of a
  second resource
- **WHEN** `isValid()` is called
- **THEN** it returns true

#### Scenario: Round trip keeps validity and equality
- **WHEN** the resource is saved and loaded into a separate `ResourceSet` with the other resources registered
- **THEN** `isValid()` is true, the content equals the saved one under `EqualityHelper`, and no proxy stays
  unresolved

#### Scenario: Query validators accept the index root
- **GIVEN** a query resource with query roots and a `QueryModelInfo` root
- **WHEN** the store's query validators (`query-validation`) run on it
- **THEN** they report no error caused by the `QueryModelInfo` root
