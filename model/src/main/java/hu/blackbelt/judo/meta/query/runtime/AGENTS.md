# `model/src/main/java/hu/blackbelt/judo/meta/query/runtime` — hand-written runtime beside the generated `QueryModel`

| File | Purpose |
|---|---|
| `QueryEpsilonValidator.java` | Runs EVL validation of a `QueryModel` through an Epsilon `ExecutionContext`. Exports three `validateQuery(Logger, QueryModel, URI scriptRoot, ...)` overloads and `calculateQueryValidationScriptURI()`. Binds the model as `QUERY`, injects `queryUtils`, resolves `query.evl` against scriptRoot, runs `parallel(true)`; context is committed and closed in `finally`. |
| `QueryUtils.java` | Flattens join graphs and renders `Select` trees for logs; implements `ModelProvider` so judo-zeta can walk a `ResourceSet`. Exports `getAllJoinsOfSelect`, `getAllJoinsOfJoin`, `formatSelect`, `getNextJoinAlias`, `getNextSubSelectAlias`, `getAllContents`. Alias helpers mutate the caller's `AtomicInteger` (`j00`/`ss00`); a revisited `Select` is cut with `...`, never recursed twice. |
| `StringUtils.java` | Left-pads strings for `QueryUtils.formatSelect` indentation, avoiding a commons-lang dependency in the Tycho plugin. Exports `EMPTY` and `leftPad(String, int, String)`; returns null on null input, returns the input unchanged when size is not greater than length, and substitutes a space when padStr is empty. `PAD_LIMIT` 8192 selects the char path. |
