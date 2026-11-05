# Tasks

Module-level tasks of the master change `openspec/changes/persisted-query-model` (judo-ng superproject) for
`judo-meta-query`, rank 4.

## 1. judo-meta-query (rank 4) — `QueryModelInfo` metamodel

- [x] 1.1 Add `QueryModelInfo` to `models/judo-meta-query/model/model/query.ecore` (D2): stamps, per-file digests, counters, entries (`QueryEntry`, `NavigationEntry`, `DataQueryEntry`, `EntityTypeExpressionsEntry` with getter-attribute / getter-reference entries whose `expression` is a non-containment **`EObject`**, so there is no dependency on judo-meta-expression), ordered relations, static entity sets. All references are non-containment; `select` / `subSelect` keep the roots as roots. Verify: the module builds via `./project.py`, `project-meta.yml` dependencies are unchanged, and the generated builders exist.
- [x] 1.2 Add a unit test: a `QueryModelInfo` referencing ASM `EClass` / `EReference` / `EAttribute`, expression objects in a second resource and query roots passes `QueryModelResourceSupport.isValid()`, saves and reloads with equality and resolved references. Verify: the test passes, and the store's query validators (`query-validation`) accept a resource with the `QueryModelInfo` root.
- [x] 1.3 Add a `query-metamodel` spec delta in the `judo-meta-query` store, and update the `query.ecore` row in the nearest `AGENTS.md` (`See change: persisted-query-model`). Verify: `openspec validate` in that store.

## 2. Folded tests (test-plan.md)

- [x] 2.1 Add unit test: QueryModelInfo with EObject expression refs valid — copy harness glue from models/judo-meta-query/model-test/src/test/java/hu/blackbelt/judo/meta/query/AbstractQueryValidationTest.java; Triple: `QueryModelInfo` referencing ASM objects, query roots and `EObject`s of a second resource / `isValid()`, save, reload / `isValid()` true; reload `EqualityHelper`-equal; 0 unresolved proxies (test-plan #TP-17)
