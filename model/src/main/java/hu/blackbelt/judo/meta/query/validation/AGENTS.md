# `model/src/main/java/hu/blackbelt/judo/meta/query/validation` — Java (judo-zeta) validation path, mirror of the EVL scripts

| File | Purpose |
|---|---|
| `QueryValidationConstants.java` | Single naming home shared by the EVL and Java engines: constraint, guard, critique and satisfies names live here so both report identical `constraintName` strings. `final`, private constructor, no instantiation. Body is currently only section comments — no name is declared yet, pending JNG-4275. Adding a rule name anywhere else breaks EVL/Java result comparison in `AbstractQueryValidationTest`. |
| `QueryValidator.java` | Java counterpart of `QueryEpsilonValidator`, driving judo-zeta `ValidationExecutor` over every `EObject` reachable from `queryModel.getResourceSet()`. Exports three `validateQuery(Logger, QueryModel, ...)` overloads (last one takes `parallel`) and nested `QueryValidationException` carrying the offending `QueryModel`. → see `QueryValidator.java.AGENTS.md` |
