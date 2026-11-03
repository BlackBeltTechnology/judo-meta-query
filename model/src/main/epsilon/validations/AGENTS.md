# `model/src/main/epsilon/validations` — EVL rule scripts loaded by the Epsilon validation path

| File | Purpose |
|---|---|
| `query-plugin-validation.evl` | Eclipse-plugin entry script for EVL validation, referenced from `plugin.xml`. Imports `query.evl` and adds a `pre` block binding `queryUtils` to `new Native("hu.blackbelt.judo.meta.query.runtime.QueryUtils")(QUERY.resource.resourceSet, false)`. Constrains the model variable name to `QUERY` and requires `QueryUtils` to keep its `(ResourceSet, boolean)` constructor — renaming either breaks in-IDE validation only, not the programmatic `QueryEpsilonValidator` path. |
| `query.evl` | Rule body loaded by `QueryEpsilonValidator.validateQuery` via `calculateQueryValidationScriptURI()`. Currently holds only `// TODO: JNG-4275` — zero constraints, so EVL validation of any Query model passes vacuously and `QueryValidationTest.testEmptyModelValidates` is trivially green. Adding rules here also changes what the Java `QueryValidator` must mirror; the two engines are asserted equal by the parameterized tests. |
