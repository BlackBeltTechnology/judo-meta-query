# `model/src/workflow` — MWE2 generation workflow that produces `model/src-gen`

| File | Purpose |
|---|---|
| `generateModel.mwe2` | MWE2 `Workflow` named `QueryModelBuilder` driving code generation from `query.genmodel`. Runs `StandaloneSetup`, `DirectoryCleaner` on `src-gen`, `EcoreGenerator`, then the JUDO generators `HelperGeneratorWorkflow`, `BuilderGeneratorWorkflow` (`QueryBuilders` and friends) and `RuntimeModelGeneratorWorkflow` (`QueryModel`). Resolves through `platform:/resource/hu.blackbelt.judo.meta.query.model`, so the Eclipse bundle symbolic name in `META-INF/MANIFEST.MF` must stay in sync. `DirectoryCleaner` wipes `src-gen` first — anything hand-written there is destroyed on regeneration. Note `EcoreGenerator.srcPath` points at `src/main/java`, not `src-gen`. |
