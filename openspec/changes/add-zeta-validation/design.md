# Design: Zeta Validation Framework Integration

## Context

The Query metamodel currently uses EVL (Epsilon Validation Language) for model validation. The existing EVL rules are minimal with placeholder TODOs (JNG-4275). This provides an opportunity to:
1. Implement proper validation rules
2. Introduce the Zeta Java validation framework
3. Run both EVL and Java validation in parallel for parity testing

This design follows the proven pattern from `judo-meta-esm` which successfully implemented dual validation.

## Goals / Non-Goals

### Goals
- Implement Java validation using Zeta framework
- Maintain EVL validation for backward compatibility
- Run both validators with same test cases (parameterized tests)
- Use constants for all constraint names, guard methods, critique names
- Document all validation rules
- Add performance benchmarks (10,000 element model)

### Non-Goals
- Replace EVL entirely (keep both running)
- Add new validation rules beyond what EVL defines
- Change the Query metamodel itself

## Architecture

### Package Structure

```
model/src/main/java/hu/blackbelt/judo/meta/query/
├── validation/
│   ├── QueryValidator.java              # Entry point
│   ├── QueryValidationConstants.java    # All constraint name constants
│   ├── rules/
│   │   ├── node/
│   │   │   └── NodeValidations.java
│   │   ├── select/
│   │   │   └── SelectValidations.java
│   │   ├── feature/
│   │   │   └── FeatureValidations.java
│   │   ├── function/
│   │   │   └── FunctionValidations.java
│   │   └── ...
│   └── extensions/
│       └── QueryElementExtensions.java  # Reusable helpers
```

### Test Structure

```
model-test/src/test/java/hu/blackbelt/judo/meta/query/
├── ValidatorType.java                   # EVL | JAVA enum
├── AbstractQueryValidationTest.java     # Base class for dual validation
├── QueryValidationNodeTest.java         # Parameterized tests
├── QueryValidationSelectTest.java
├── QueryValidationPerformanceTest.java  # 10,000 element benchmark
└── ...
```

## Decisions

### Decision 1: Use Zeta Validation Framework
**Rationale:**
- Zeta is the established validation framework for Judo metamodels
- Already proven in judo-meta-esm
- Provides annotation-based rule definition (`@Constraint`, `@Critique`, `@Guard`)
- Supports parallel execution for large models
- Built-in caching for `satisfies()` calls

**Alternatives considered:**
- Custom validation framework: More work, less proven
- Only EVL: No IDE support, harder to debug

### Decision 2: Dual Validation (EVL + Java)
**Rationale:**
- Allows incremental migration
- Provides verification that Java matches EVL behavior
- Same tests validate both implementations

**Implementation:**
- `ValidatorType` enum selects which validator to run
- `@ParameterizedTest` with `@EnumSource(ValidatorType.class)`
- `AbstractQueryValidationTest.runValidation()` dispatches to appropriate validator

### Decision 3: Use Constants for All Identifiers
**Rationale:**
- Prevents typos in constraint names
- Enables refactoring
- Provides single source of truth

**Implementation:**
```java
public final class QueryValidationConstants {
    // Constraint names
    public static final String SELECT_MUST_HAVE_FROM = "SelectMustHaveFrom";
    public static final String NODE_MUST_HAVE_TYPE = "NodeMustHaveType";

    // Guard method names
    public static final String GUARD_HAS_NAME = "hasName";

    // Critique names
    public static final String CRITIQUE_SHOULD_HAVE_ALIAS = "ShouldHaveAlias";
}
```

### Decision 4: Convert EVL Rules to Java

**EVL Pattern:**
```evl
context Select {
    constraint SelectMustHaveFrom {
        guard: self.from.isDefined()
        check: self.from.isKindOf(EClass)
        message: "Select must have a valid 'from' reference"
    }
}
```

**Java Pattern:**
```java
@ValidationContext(Select.class)
public class SelectValidations {

    @Constraint(
        name = QueryValidationConstants.SELECT_MUST_HAVE_FROM,
        message = "Select must have a valid 'from' reference"
    )
    @Guard(method = QueryValidationConstants.GUARD_FROM_DEFINED)
    public ValidationRule selectMustHaveFrom() {
        return (element, ctx) -> {
            Select select = (Select) element;
            return select.getFrom() instanceof EClass
                ? ValidationResult.pass()
                : ValidationResult.fail("Select 'from' must be an EClass");
        };
    }

    private boolean fromDefined(EObject element, ValidationContext ctx) {
        return ((Select) element).getFrom() != null;
    }
}
```

### Decision 5: Performance Test with 10,000 Elements
**Rationale:**
- Validates parallel execution benefits
- Provides baseline metrics
- Identifies performance regressions

**Implementation:**
```java
@Test
void performanceComparison() {
    QueryModel model = generateLargeModel(10_000);

    long evlTime = measureEvlValidation(model);
    long javaTime = measureJavaValidation(model);

    log.info("EVL: {}ms, Java: {}ms, Speedup: {}x",
             evlTime, javaTime, (double)evlTime/javaTime);
}
```

## Dependency Configuration

### Parent pom.xml Property
```xml
<properties>
    <judo-zeta-version>1.0.0-SNAPSHOT</judo-zeta-version>
</properties>
```

### Model Module Dependencies
```xml
<dependency>
    <groupId>hu.blackbelt.judo.zeta</groupId>
    <artifactId>hu.blackbelt.judo.zeta.validation-core</artifactId>
    <version>${judo-zeta-version}</version>
</dependency>
<dependency>
    <groupId>hu.blackbelt.judo.zeta</groupId>
    <artifactId>hu.blackbelt.judo.zeta.zeta-annotations</artifactId>
    <version>${judo-zeta-version}</version>
</dependency>
<dependency>
    <groupId>hu.blackbelt.judo.zeta</groupId>
    <artifactId>hu.blackbelt.judo.zeta.zeta-common</artifactId>
    <version>${judo-zeta-version}</version>
</dependency>
```

## Validation Rules to Implement

Based on analysis of `query.evl` and `query-plugin-validation.evl`:

| Context | Constraint Name | Description |
|---------|-----------------|-------------|
| Select | SelectMustHaveFrom | Select must have valid 'from' reference |
| Node | NodeMustHaveType | Node getType() must return valid EClass |
| SubSelect | SubSelectMustHaveBase | SubSelect must have base navigation |
| Join | JoinMustHaveBase | Join getBase() must be valid |
| Feature | FeatureMustHaveTarget | Feature must have target mapping |
| Function | FunctionMustHaveSignature | Function must have valid signature |
| Filter | FilterMustHaveExpression | Filter must have valid expression |
| OrderBy | OrderByMustHaveFeature | OrderBy must reference a feature |
| Variable | VariableMustHaveType | Variable must have type defined |
| Constant | ConstantMustHaveValue | Constant must have value |

*Note: Full rule list to be extracted from EVL files during implementation*

## Documentation Structure

```
docs/
├── validation/
│   ├── README.md                    # Overview and getting started
│   ├── java-validation-framework.md # Zeta framework reference
│   ├── query-validation-rules.md    # All implemented rules
│   └── migration-guide.md           # EVL to Java migration
```

## Risks / Trade-offs

### Risk: EVL and Java Results Diverge
**Mitigation:**
- Run both validators on every test
- Compare constraint names (not full messages)
- Fail tests on any mismatch

### Risk: Performance Regression
**Mitigation:**
- Performance test with 10,000 elements
- Set baseline thresholds
- Fail CI if exceeded

### Trade-off: Maintenance of Two Validation Systems
**Rationale:**
- Short-term cost for long-term benefit
- EVL can be deprecated once Java proven stable
- Parameterized tests minimize test duplication

## Open Questions

1. Should we add new validation rules beyond what EVL currently defines?
   - **Recommendation:** No, keep parity first, add new rules in separate change

2. Should the performance test be in main test suite or separate profile?
   - **Recommendation:** Separate Maven profile (`-Pperformance-test`) to avoid CI slowdown

## Migration Plan

1. **Phase 1:** Add infrastructure (dependencies, base classes, constants)
2. **Phase 2:** Implement validation rules (mirror EVL)
3. **Phase 3:** Convert existing tests to parameterized
4. **Phase 4:** Add performance test
5. **Phase 5:** Update documentation
6. **Phase 6:** Convert adoc files to markdown (except pages/)

No rollback needed - EVL remains primary until Java is proven.
