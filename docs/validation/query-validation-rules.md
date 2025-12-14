# Query Validation Rules

This document lists all validation rules implemented for the Query metamodel.

## Status

**Current status:** Infrastructure complete, rules pending (JNG-4275)

The validation framework is in place with both EVL and Java validators ready. Specific validation rules will be added as part of JNG-4275.

## Validation Rule Reference

### Legend

| Symbol | Meaning |
|--------|---------|
| ERROR | Constraint - validation fails if rule violated |
| WARNING | Critique - advisory message, validation continues |
| PLANNED | Rule designed but not yet implemented |

### Node Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| NodeMustHaveType | ERROR | Node `getType()` must return valid EClass | PLANNED |

### Select Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| SelectMustHaveFrom | ERROR | Select must have a valid 'from' EClass reference | PLANNED |
| SelectMustHaveMainTarget | ERROR | Select must have a main target | PLANNED |
| SelectShouldHaveAlias | WARNING | Select should have an alias for clarity | PLANNED |

### SubSelect Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| SubSelectMustHaveBase | ERROR | SubSelect must have a base navigation | PLANNED |
| SubSelectMustHaveSelect | ERROR | SubSelect must reference a Select | PLANNED |

### Join Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| JoinMustHaveBase | ERROR | Join `getBase()` must return valid base | PLANNED |
| JoinMustHaveAlias | ERROR | Join must have an alias | PLANNED |

### Feature Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| FeatureMustHaveTargetMapping | ERROR | Feature must have target mapping | PLANNED |

### Function Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| FunctionMustHaveSignature | ERROR | Function must have valid signature | PLANNED |
| FunctionParameterCountMatch | ERROR | Function parameters must match signature | PLANNED |

### Filter Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| FilterMustHaveExpression | ERROR | Filter must have a valid expression | PLANNED |

### OrderBy Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| OrderByMustHaveFeature | ERROR | OrderBy must reference a feature | PLANNED |
| OrderByDirectionValid | ERROR | OrderBy direction must be ASC or DESC | PLANNED |

### Variable Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| VariableMustHaveType | ERROR | Variable must have type defined | PLANNED |
| VariableMustHaveName | ERROR | Variable must have a name | PLANNED |

### Constant Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| ConstantMustHaveValue | ERROR | Constant must have a value | PLANNED |

### Target Validations

| Rule Name | Severity | Description | Status |
|-----------|----------|-------------|--------|
| TargetIndexUnique | ERROR | Target index must be unique within select | PLANNED |

## Adding New Rules

When implementing a new rule:

1. Add the constant to `QueryValidationConstants.java`
2. Create/update the validation class in `validation/rules/`
3. Register the class in `QueryValidator.java`
4. Add the corresponding EVL rule to `query.evl`
5. Write parameterized tests in `model-test`
6. Update this document

## See Also

- [Java Validation Framework](java-validation-framework.md) - How to implement rules
- [Query Metamodel](../README.md) - Query model documentation
