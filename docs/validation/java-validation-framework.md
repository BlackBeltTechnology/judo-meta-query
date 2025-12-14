# Java Validation Framework

The Query model uses the [Judo Zeta Validation Framework](https://github.com/BlackBeltTechnology/judo-zeta) for Java-based validation.

## Overview

The Zeta validation framework provides:

- **Annotation-based rule definition** - Use `@Constraint`, `@Critique`, `@Guard`, `@Satisfies`
- **Parallel execution** - Validate large models efficiently
- **Caching** - Automatic caching for `satisfies()` calls
- **Extension methods** - Reusable helper methods across validators

## Package Structure

```
hu.blackbelt.judo.meta.query.validation/
├── QueryValidator.java              # Entry point
├── QueryValidationConstants.java    # All constraint name constants
├── rules/                           # Validation rule classes
│   ├── node/
│   │   └── NodeValidations.java
│   ├── select/
│   │   └── SelectValidations.java
│   └── ...
└── extensions/                      # Extension method classes
    └── QueryElementExtensions.java
```

## Writing Validation Rules

### Basic Constraint

```java
@ValidationContext(Select.class)
public class SelectValidations {

    @Constraint(
        name = QueryValidationConstants.SELECT_MUST_HAVE_FROM,
        message = "Select must have a valid 'from' reference"
    )
    public ValidationRule selectMustHaveFrom() {
        return (element, ctx) -> {
            Select select = (Select) element;
            if (select.getFrom() == null) {
                return ValidationResult.fail(
                    "Select '" + select.getAlias() + "' has no 'from' reference"
                );
            }
            return ValidationResult.pass();
        };
    }
}
```

### With Guard

```java
@Constraint(
    name = QueryValidationConstants.JOIN_MUST_HAVE_ALIAS,
    message = "Join must have an alias"
)
@Guard(method = QueryValidationConstants.GUARD_JOIN_HAS_BASE)
public ValidationRule joinMustHaveAlias() {
    return (element, ctx) -> {
        Join join = (Join) element;
        if (join.getAlias() == null || join.getAlias().isEmpty()) {
            return ValidationResult.fail("Join has no alias");
        }
        return ValidationResult.pass();
    };
}

private boolean joinHasBase(EObject element, ValidationContext ctx) {
    return ((Join) element).getBase() != null;
}
```

### With Satisfies (Dependencies)

```java
@Constraint(
    name = QueryValidationConstants.TARGET_MUST_HAVE_VALID_INDEX,
    message = "Target index must be unique within select"
)
@Satisfies({QueryValidationConstants.SELECT_MUST_HAVE_FROM})
public ValidationRule targetMustHaveValidIndex() {
    return (element, ctx) -> {
        Target target = (Target) element;
        // This only runs if SELECT_MUST_HAVE_FROM passed
        // ...
        return ValidationResult.pass();
    };
}
```

### Critique (Warning)

```java
@Critique(
    name = QueryValidationConstants.CRITIQUE_SELECT_SHOULD_HAVE_ALIAS,
    message = "Select should have an alias for clarity"
)
public ValidationRule selectShouldHaveAlias() {
    return (element, ctx) -> {
        Select select = (Select) element;
        if (select.getAlias() == null || select.getAlias().isEmpty()) {
            return ValidationResult.fail("Select has no alias");
        }
        return ValidationResult.pass();
    };
}
```

## Using Constants

All constraint names, guard method names, and critique names **must** be defined in `QueryValidationConstants`:

```java
public final class QueryValidationConstants {

    // Constraint names
    public static final String SELECT_MUST_HAVE_FROM = "SelectMustHaveFrom";
    public static final String JOIN_MUST_HAVE_ALIAS = "JoinMustHaveAlias";

    // Guard method names
    public static final String GUARD_JOIN_HAS_BASE = "joinHasBase";

    // Critique names
    public static final String CRITIQUE_SELECT_SHOULD_HAVE_ALIAS = "SelectShouldHaveAlias";
}
```

## Registering Validators

In `QueryValidator.java`:

```java
// Register validation rule classes
registry.register(SelectValidations.class);
registry.register(JoinValidations.class);
registry.register(NodeValidations.class);
// ... etc
```

## Testing

Use parameterized tests to run against both EVL and Java validators:

```java
@ParameterizedTest(name = "{0}")
@EnumSource(ValidatorType.class)
void testSelectMustHaveFrom(ValidatorType validatorType) throws Exception {
    // Create invalid model
    Select select = queryModel.getQueryFactory().createSelect();
    queryModel.getResource().getContents().add(select);

    // Run validation expecting the constraint to fail
    runValidation(
        validatorType,
        List.of(QueryValidationConstants.SELECT_MUST_HAVE_FROM),
        Collections.emptyList()
    );
}
```

## Performance

The Java validator supports parallel execution for large models:

```java
// Enable parallel execution (for models > 5000 elements)
QueryValidator.validateQuery(log, queryModel, null, null, true);
```

Performance tests are available in `QueryValidationPerformanceTest`.

## See Also

- [Judo Zeta Repository](https://github.com/BlackBeltTechnology/judo-zeta) - Source code and full documentation
- [Query Validation Rules](query-validation-rules.md) - List of implemented rules
