# judo-meta-query — module agent doctrine

## Module purpose

`judo-meta-query` owns the **Query metamodel**: the EMF/Ecore language in which
JUDO expresses a *resolved, relational-shaped read plan* — the intermediate form
that sits between a JQL/expression model and actual SQL. A query model is a tree
of `Select` nodes carrying `Target`s, `Feature`s (`Attribute`, `Constant`,
`Function`, `IdAttribute`, `TypeAttribute`, `EntityTypeName`), `Filter`s,
`OrderBy`s and a join graph (`ReferencedJoin`, `SubSelectJoin`, `ContainerJoin`,
`CastJoin`, `CustomJoin`) plus `SubSelect` nodes for aggregation and pagination.
It is *not* a user-facing DSL: instances are produced by judo-tatami
transformations from ASM/expression models, and consumed downstream by
`judo-runtime-core-query` and `judo-runtime-core-dao-rdbms`, which walk the join
graph to emit vendor-specific RDBMS statements.

What the module actually ships: the `query.ecore`/`query.genmodel` pair as the
source of truth, EMF classes + fluent builders + `QueryModel` loader/saver
generated from it by an MWE2 workflow, a small hand-written runtime
(`QueryUtils`, `QueryEpsilonValidator`, `StringUtils`), Epsilon EVL validation
rules, and three delivery shapes of the same metamodel — an Eclipse plugin
(with feature and P2 update site), a plain Maven jar, and an OSGi bundle.

- **Repository:** BlackBeltTechnology/judo-meta-query
- **Group/artifact:** `hu.blackbelt.judo.meta:hu.blackbelt.judo.meta.query`, packaging `pom` (aggregator), version `${revision}` (`1.0.3-SNAPSHOT`)
- **License:** Eclipse Public License 2.0 (EPL-2.0)
- **Java:** 21 (JavaSE-21) · **Build:** Maven 3.9.4+ with Tycho 4.0.13
- Part of the [judo-community](https://github.com/BlackBeltTechnology/judo-community) ecosystem

## Reactor map

The root POM is an aggregator; `<modules>` live in the `modules` profile, which
is **active by default** and can be switched off with `-DskipModules=true`.
Every submodule shares the artifactId prefix `hu.blackbelt.judo.meta.query`.

<modules>
  <module>model</module>
  <module>model-test</module>
  <module>osgi</module>
  <module>osgi-itest</module>
  <module>feature</module>
  <module>site</module>
</modules>

| Module | Packaging | Contributes |
|---|---|---|
| `model` | `eclipse-plugin` | The metamodel itself: `model/query.ecore` + `query.genmodel`, the `src/workflow/generateModel.mwe2` generation pipeline, the generated EMF code under `src-gen/`, the hand-written `runtime`/`validation` Java, the EVL rule set, `plugin.xml` and the OSGi `MANIFEST.MF`. Everything else in the reactor packages or verifies this one artifact. |
| `model-test` | `jar` | JUnit 5 verification of the shipped metamodel — load/save round-trips, builder API, EVL validation wiring and a validation performance guard. Test-only; not deployed as a consumable API. |
| `osgi` | `bundle` (maven-bundle-plugin) | Repackages the Eclipse-plugin metamodel as a plain OSGi bundle with Declarative Services metadata, so non-Eclipse (Karaf/runtime) containers can consume the model without P2. |
| `osgi-itest` | `jar` | Pax Exam 4.13.5 + Karaf 4.4.7 integration tests that provision the `osgi` bundle in a real container and assert the bundle actually resolves and registers its services. |
| `feature` | `eclipse-feature` | Packaging-only: the Eclipse feature descriptor that groups the `model` plugin (and its dependencies) into one installable unit. No source. |
| `site` | `eclipse-repository` | Packaging-only: builds the P2 update site from `feature`, i.e. the artifact an Eclipse IDE user actually points at to install metamodel editor support. |

Related but **outside the reactor:** `targetdefinition/` (Eclipse target
platform definition used by the IDE, not a Maven module), `docs/`, `openspec/`
and `.github/workflows/` (CI: `build.yml`, `release.yml`,
`merge-pr-tagged.yml`).

## Build commands

Use the bundled Maven wrapper (`./mvnw`) from this module directory. Never run a
build from the judo-ng repo root.

```bash
# Full build (all modules)
./mvnw clean install

# Run all tests
./mvnw clean test

# Run a single test class
./mvnw test -pl model-test -Dtest=QueryValidationTest

# Skip tests
./mvnw clean install -DskipTests

# Code coverage
./mvnw clean test jacoco:report

# Update Eclipse site category versions
mvn clean install -P update-category-versions -f site/pom.xml
```

### Maven profiles

| Profile | Purpose |
|---|---|
| `modules` | Active by default — includes all submodules. Deactivate with `-DskipModules=true` |
| `sign-artifacts` | Signs artifacts using `sign-maven-plugin` (CI releases) |
| `release-dummy` | Deploys to local `/tmp/` directory for testing |
| `release-judong` | Deploys to Judong Nexus snapshot repository |
| `release-central` | Deploys to Maven Central via Sonatype OSSRH |
| `generate-github-asciidoc-diagrams` | Generates PNG diagrams from PlantUML in AsciiDoc |
| `update-source-code-license` | Updates EPL-2.0 license headers in source files |

`flatten-maven-plugin` 1.3.0 resolves `${revision}` into the deployed POMs
(CI-friendly versions).

## Technology stack

- **Eclipse EMF (Ecore)** — metamodel framework; `org.eclipse.emf.ecore` 2.38.0, `org.eclipse.emf.common` 2.41.0
- **Epsilon Runtime 2.8.0** — model validation via EVL (Epsilon Validation Language)
- **Xtext 2.39.0 / MWE2** — code generation workflows
- **OSGi** — `osgi.core` 7.0.0, `osgi.cmpn` 7.0.0
- **Tycho 4.0.13** — Eclipse plugin building, version management, P2 metadata
- **JUnit Jupiter 5.9.1** · **Pax Exam 4.13.5** (Karaf 4.4.7) · **JaCoCo 0.8.12**
- **SLF4J 2.0.16 + Logback 1.5.12** — logging
- **Lombok 1.18.34** — non-Eclipse modules only (Tycho incompatibility)

## Architecture pointers

Source-of-truth artifacts, and where each is recorded in full — per-file detail
lives in the nearest directory `AGENTS.md`, never here:

- **Metamodel definition.** `model/model/` holds `query.ecore` (the source of truth
  for the Query model) beside its `query.genmodel` code-generation parameters.
  Editing the `.ecore` without regenerating leaves the Java surface stale.
- **Generation pipeline.** `model/src/workflow/generateModel.mwe2` orchestrates the
  MWE2 run that turns the `.ecore` into the generated EMF classes.
- **Eclipse/OSGi packaging.** `model/META-INF/MANIFEST.MF` declares the bundle, and
  `model/plugin.xml` registers the Eclipse extensions — EVL validation, EMF parsers,
  and the utility contributions.
- **Validation rules.** `model/src/main/epsilon/validations/` carries `query.evl` (the
  main Epsilon rule set) and `query-plugin-validation.evl` (the rules that apply only
  inside the Eclipse plugin).

The module-root `pom.xml` is the aggregator: it owns the `revision` property,
the dependency BOMs and shared plugin configuration for every submodule. The
module-root `logback-test.xml` is the shared SLF4J/Logback test logging
configuration picked up by the test modules.

## Development environment

**Required:** Java 21 JDK, Maven 3.9.4+ (or use `./mvnw`).

**Eclipse IDE (optional):** m2e, Epsilon and Modeling Tools plugins; Xtext, MWE
and MWE2 plugins for code generation. Install the metamodel via the P2 update
site for editor support.

## Git workflow

- **Main branch:** `develop`
- **Versioning:** `revision` property (currently `1.0.3-SNAPSHOT`); Tycho bridges Maven SNAPSHOT ↔ Eclipse `.qualifier`
- **Branching:** GitFlow — `feature/JNG-*`, `release/*`, `bugfix/JNG-*`, `support/JNG-*`, `hotfix/JNG-*`
- **CI/CD:** GitHub Actions — `build.yml` (main pipeline), `release.yml` (manual releases), `merge-pr-tagged.yml` (auto-merge)
- **Rule:** every commit must reference a JIRA ticket (`JNG-xxx`)

## Code instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Scope guard

1. **Never edit `model/src-gen/`** — all code there is generated from `query.ecore` via the MWE2 workflow. Regenerate after model changes.
2. **No Lombok in Eclipse plugin modules** — Tycho does not support Lombok annotation processing. Only use Lombok in `model-test/`, `osgi/`, and `osgi-itest/`.
3. **Hand-written runtime code** lives in `model/src/main/java/hu/blackbelt/judo/meta/query/runtime/`:
   - `QueryUtils` — join alias formatting, recursive join collection, select tree formatting
   - `QueryEpsilonValidator` — programmatic EVL validation runner
   - `StringUtils` — string padding helper
4. **Generated code** includes `QueryModel` (model loader with fluent builder API) and `QueryModelResourceSupport` (EMF ResourceSet factory) in the `runtime` and `support` packages respectively.
5. **Validation rules** are currently minimal (`query.evl` contains only a TODO comment for JNG-4275). The `query-plugin-validation.evl` provides Eclipse-specific constraints.
6. **Version updates** must be applied through the `revision` property in the root `pom.xml`. Do not change individual module versions directly.

## Related documentation

- [README.md](README.md) — project overview with metamodel class diagram
- [CONTRIBUTING.md](CONTRIBUTING.md) — development setup, build lifecycle, code generation details
- [.github/CIFLOW.md](.github/CIFLOW.md) — branching strategy and CI/CD workflow documentation
- [judo-community](https://github.com/BlackBeltTechnology/judo-community) — parent ecosystem documentation

<!-- dox-doctrine -->
## Documentation Update Protocol (WRITE discipline)

Per-directory `AGENTS.md` files form a tree. Each directory `AGENTS.md` is the
per-file record for the files in that directory. This module-root `AGENTS.md`
holds doctrine + architecture pointers only — never a per-file index.

**Keep the root lean.** This file loads into every agent turn — every byte costs
tokens on every turn. A verbose root file buries the rules the model must follow
(signal dilution) and measurably degrades adherence; a lean file keeps doctrine
salient. Default assumption: your update does NOT belong in the root — route it
by the table below.

**Route every doc update by kind:**

| Kind of update | Goes in |
|---|---|
| New file in a directory, or its per-file detail / change history | Nearest directory `AGENTS.md`. Add a `` | `<basename>` | <purpose> | `` row, path-alphabetical. |
| Data flow, protocol, architecture rationale | `docs/architecture.md` or a `docs/<topic>.md` |
| End-user / developer setup | `README.md` |
| Cross-cutting rule every agent needs every turn (rare) | this module-root `AGENTS.md` |

**Read before editing (chain walk).** Before editing a file, read the nearest
`AGENTS.md` chain root→leaf so you know the file's recorded purpose, contracts,
and change history. Do not edit blind.

**Update after editing (closeout pass).** After changing a file, update its row
in the nearest directory `AGENTS.md`: find the file's row, update its purpose in
place; if absent, add it in path-alphabetical order. New directory → scaffold
its `AGENTS.md`. One row per file. The purpose carries a one-line summary, key
exported symbols, contracts/invariants, and `See change: <id>` history.

**Row style (caveman).** Short declarative fragments. Drop articles. Subject →
verb → object, present tense. One fact per row. Prefer concrete tokens (paths,
symbols, env vars) over prose. Keep identifiers verbatim.

**Size rule — split an over-large directory `AGENTS.md` file-based.** pi
auto-injects a directory `AGENTS.md` on every turn when cwd sits at/below it, so
an over-large directory `AGENTS.md` is not supported. Split it file-based: a row
exceeding the length threshold promotes to a per-file `<File>.AGENTS.md`
sidecar carrying that file's full detail (including every `See change:`). The
sidecar is pull-only — its name is not `AGENTS.md`, so pi never auto-injects it
— yet it stays search-indexed (`agents` doc_type). The directory `AGENTS.md`
keeps a one-line summary plus a `→ see `<File>.AGENTS.md`` pointer. Rows within
the threshold stay verbatim (lossless).

## Finding docs (READ discipline)

`kb_*` tools are faster and cheaper than raw search — they return a one-line
purpose + key exports per file, not raw bytes. **This fires on the ACTION, not
the intent** — before you `grep`/`rg` for a symbol, `cat`/read a file to learn
what it does, or chase an import, the kb call goes first. It fires **even
mid-task when you already know the file**; knowing the file does not exempt you.
When your reflex is the left column, run the right column instead:

| You're about to… | Do this FIRST instead |
|---|---|
| `grep -rn "SymbolName" src/` — find where a fn / type / const lives | `kb_search --doc-type agents "SymbolName"` — tree indexes key exports per file |
| `grep -rn "feature\|topic" src/` — how does X work / where's X handled | `kb_search "feature topic"` |
| `cat` / read a file just to learn its purpose before editing | `kb agents <path>` — one-line purpose + exports + change history |
| chase imports / callers across files | `kb_neighbors <path\|heading>` |
| read one doc section in full | `kb_get <path> <section>` |

**Fall-through (explicit):** if the kb call returns nothing relevant, `rg` /
source read is allowed — then add the missing directory `AGENTS.md` row per the
WRITE discipline. kb does NOT replace grep; it goes first.
