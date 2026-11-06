## Why

The JUDO runtime rebuilds the logical query model from JQL on every boot. This module (`judo-meta-query`, rank 4)
owns the metamodel the persisted artifact is expressed in, so it must gain the typed index root `QueryModelInfo`
before the builder (`judo-tatami-asm2query`) and the runtimes can save and restore the model. See the master change
`openspec/changes/persisted-query-model` in the judo-ng superproject (design D2).

## What Changes

- Add `QueryModelInfo` to `model/model/query.ecore` as one root per query resource: `formatVersion`, `modelName`,
  `generator`, `asmDigest`, `measureDigest`, `expressionDigest`, `nextSourceIndex`, `nextTargetIndex`.
- Add the entry types `QueryEntry`, `NavigationEntry`, `DataQueryEntry` and `EntityTypeExpressionsEntry` (with
  getter-attribute and getter-reference entries), plus `orderedTransferRelations`, `staticEntityAttributes` and
  `staticEntityReferences`.
- All ASM-typed and select-typed references are non-containment, so query roots stay roots. The entity expression
  references are typed `EObject`, so this module gains no dependency on `judo-meta-expression`.
- Regenerate the EMF code and fluent builders from `query.genmodel` (generated, not hand-edited).
- Require `QueryModelResourceSupport.isValid()` to hold for a resource that contains the new root.
- Add a unit test for the round trip with `EObject` expression references.
- No breaking change: the new classes are additive and existing classes are untouched.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `query-metamodel`: adds the `QueryModelInfo` index root, its entry types, the non-containment and
  `EObject`-typed reference rules and the `isValid()` requirement.

## Impact

- **Files:** `model/model/query.ecore`, `model/model/query.genmodel` (if the generator needs it), regenerated
  `model/src-gen/` (never hand-edited), the new unit test in `model-test`, the nearest `AGENTS.md` row for
  `query.ecore`.
- **Dependencies:** `project-meta.yml` dependencies of `judo-meta-query` are unchanged; no `judo-meta-expression`
  dependency is introduced.
- **Rank:** 4. First module in the migration order; `judo-tatami-base` (rank 7) and every later module consume it.
- **Rollback:** revert the module commit. Nothing consumes the new classes until `judo-tatami-base` adopts them;
  existing query models stay valid because no existing class changes.
