# `QueryValidator.java`

Java counterpart of `QueryEpsilonValidator` on the judo-zeta stack, offered as the
debuggable alternative to the EVL script path.

**Exports**

- `validateQuery(Logger, QueryModel)` — delegates with null expectations, `parallel=false`.
- `validateQuery(Logger, QueryModel, Collection<String> expectedErrors, Collection<String> expectedWarnings)`.
- `validateQuery(Logger, QueryModel, Collection<String>, Collection<String>, boolean parallel)` — the real body.
- nested `QueryValidator.QueryValidationException extends Exception`, message `"Query model validation failed"`,
  exposes `getQueryModel()`.

**Wiring**

Builds a `ValidationRegistry`, an `ExtensionMethodRegistry`, and a `ValidationContext` over
`new QueryUtils()` (as `ModelProvider`) plus `queryModel.getResourceSet()`, then runs a
`ValidationExecutor`. Elements to validate are every `EObject` yielded by `getAllContents()` of every
resource in the ResourceSet — the full set, not a filtered subset.

**Contracts a caller can violate**

- The registry is registered with **no rule classes** (rules are TODO, JNG-4275), so validation of any
  model currently returns zero results and passes vacuously. Registration must happen inside this
  method — a rule class not registered here never runs.
- Failures are split by `Severity.ERROR` / `Severity.WARNING`. Warnings are only logged, never thrown.
- When `expectedErrors` is null, ANY error throws `QueryValidationException`. When it is non-null, the
  actual/expected `constraintName` sets must match exactly; a mismatch (unexpected or missing, in
  either errors or warnings) sets `hasUnexpected` and throws.
- `expectedWarnings` is compared only when non-null; passing null suppresses warning comparison while
  still comparing errors.
- Comparison keys are `ValidationResult.getConstraintName()`; a result with a null constraint name is
  dropped from the comparison sets and can therefore never be "expected".
- `executor.shutdown()` runs in `finally`, so the parallel executor is released even on throw. A caller
  that bypasses this method and drives `ValidationExecutor` directly must shut it down itself.
