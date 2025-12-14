# Zeta Validation Framework Skill

This skill helps create and describe proper validation rules and tests for the Judo Zeta Validation Framework.

## Overview

The Judo Zeta Validation Framework is an annotation-based validation system for Eclipse Modeling Framework (EMF) metamodels. It replaces Epsilon Validation Language (EVL) with type-safe Java implementations.

**Repository**: https://github.com/BlackBeltTechnology/judo-zeta
**Documentation**: https://github.com/BlackBeltTechnology/judo-zeta/tree/develop/docs/validation

## Core Annotations

All annotations are in package `hu.blackbelt.judo.zeta.annotation`:

| Annotation | Purpose |
|------------|---------|
| `@ValidationContext` | Marks a class as containing validation rules for a specific EMF element type |
| `@Constraint` | Defines an error-level validation rule (ERROR severity) |
| `@Critique` | Defines a warning-level validation rule (WARNING severity) |
| `@Guard` | Conditionally enables a rule based on a guard method |
| `@Satisfies` | Declares dependencies on other validation rules |

## ValidationRule Pattern

All validation methods must return `ValidationRule` - a functional interface:

```java
@FunctionalInterface
public interface ValidationRule {
    ValidationResult apply(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext context);
}
```

## Basic Structure

**IMPORTANT**: Always use constants for constraint names, critique names, and @Satisfies references.

```java
package hu.blackbelt.judo.meta.psm.validation.rules.<domain>;

import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Critique;
import hu.blackbelt.judo.zeta.annotation.Satisfies;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import hu.blackbelt.judo.zeta.validation.core.Severity;

@ValidationContext(YourElementType.class)
public class YourElementTypeValidations {

    // Constraint/Critique name constants
    private static final String CONSTRAINT_NAME = "ConstraintName";
    private static final String ANOTHER_CONSTRAINT = "AnotherConstraint";
    // External constraint references (from other validation classes)
    private static final String NAMED_ELEMENT_HAS_CONTAINER = "NamedElementHasContainer";

    @Constraint(name = CONSTRAINT_NAME, message = "Error message description")
    public ValidationRule constraintName() {
        return (element, context) -> {
            YourElementType self = (YourElementType) element;
            
            if (/* validation fails */) {
                return ValidationResult.fail(
                    CONSTRAINT_NAME,
                    "Detailed error message for: " + self.getName(),
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }

    @Constraint(name = ANOTHER_CONSTRAINT, message = "Another constraint")
    @Satisfies(NAMED_ELEMENT_HAS_CONTAINER)
    public ValidationRule anotherConstraint() {
        return (element, context) -> {
            // ...
            return ValidationResult.pass();
        };
    }
}
```

### Constants Naming Convention

Convert constraint names from PascalCase to UPPER_SNAKE_CASE:

| Constraint Name | Constant Name |
|-----------------|---------------|
| `ElementNameNotEmpty` | `ELEMENT_NAME_NOT_EMPTY` |
| `InheritanceIsNotRecursive` | `INHERITANCE_IS_NOT_RECURSIVE` |
| `CardinalityUpperIsAtLeastOne` | `CARDINALITY_UPPER_IS_AT_LEAST_ONE` |

## Validation Patterns

### 1. Simple Existence Check

```java
private static final String ELEMENT_NAME_NOT_EMPTY = "ElementNameNotEmpty";

@Constraint(name = ELEMENT_NAME_NOT_EMPTY, message = "Name must not be empty")
public ValidationRule elementNameNotEmpty() {
    return (element, context) -> {
        NamedElement self = (NamedElement) element;
        if (self.getName() == null || self.getName().isEmpty()) {
            return ValidationResult.fail(
                ELEMENT_NAME_NOT_EMPTY,
                "Named element name must contain at least one character",
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

### 2. Pattern Validation

```java
private static final String ELEMENT_NAME_CONTAINS_VALID_CHARACTERS = "ElementNameContainsValidCharacters";
private static final String ELEMENT_NAME_NOT_EMPTY = "ElementNameNotEmpty";
private static final Pattern VALID_NAME_PATTERN = Pattern.compile("([a-z]|[A-Z]|[0-9]|_)+");

@Constraint(name = ELEMENT_NAME_CONTAINS_VALID_CHARACTERS, message = "Name contains invalid characters")
@Satisfies(ELEMENT_NAME_NOT_EMPTY)
public ValidationRule elementNameContainsValidCharacters() {
    return (element, context) -> {
        NamedElement self = (NamedElement) element;
        if (self.getName() == null || self.getName().isEmpty()) {
            return ValidationResult.pass();
        }

        if (!VALID_NAME_PATTERN.matcher(self.getName()).matches()) {
            return ValidationResult.fail(
                ELEMENT_NAME_CONTAINS_VALID_CHARACTERS,
                "Name can only contain letters, digits and underscores: " + self.getName(),
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

### 3. Uniqueness Validation

```java
private static final String NAMED_ELEMENT_IS_UNIQUE_IN_ITS_CONTAINER = "NamedElementIsUniqueInItsContainer";
private static final String NAMED_ELEMENT_HAS_CONTAINER = "NamedElementHasContainer";

@Constraint(name = NAMED_ELEMENT_IS_UNIQUE_IN_ITS_CONTAINER, message = "Element is not unique")
@Satisfies(NAMED_ELEMENT_HAS_CONTAINER)
public ValidationRule namedElementIsUniqueInItsContainer() {
    return (element, context) -> {
        NamedElement self = (NamedElement) element;
        if (self.eContainer() == null) {
            return ValidationResult.pass();
        }

        boolean hasDuplicate = self.eContainer().eContents().stream()
            .filter(c -> c instanceof NamedElement)
            .map(c -> (NamedElement) c)
            .filter(ne -> ne != self)
            .anyMatch(ne -> ne.getName() != null && self.getName() != null
                    && ne.getName().equalsIgnoreCase(self.getName()));

        if (hasDuplicate) {
            return ValidationResult.fail(
                NAMED_ELEMENT_IS_UNIQUE_IN_ITS_CONTAINER,
                "Named element " + self.getName() + " is not unique in its container",
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

### 4. Cross-Model Uniqueness

```java
private static final String ENTITY_TYPE_NAMES_ARE_UNIQUE = "EntityTypeNamesAreUnique";
private static final String NAMED_ELEMENT_IS_UNIQUE_IN_ITS_CONTAINER = "NamedElementIsUniqueInItsContainer";

@Critique(name = ENTITY_TYPE_NAMES_ARE_UNIQUE, message = "Entity type name is not unique")
@Satisfies(NAMED_ELEMENT_IS_UNIQUE_IN_ITS_CONTAINER)
public ValidationRule entityTypeNamesAreUnique() {
    return (element, context) -> {
        EntityType self = (EntityType) element;
        boolean hasDuplicate = context.getAllInstances(EntityType.class).stream()
            .filter(e -> e != self)
            .anyMatch(e -> e.getName() != null && self.getName() != null
                    && e.getName().equalsIgnoreCase(self.getName()));

        if (hasDuplicate) {
            return ValidationResult.fail(
                ENTITY_TYPE_NAMES_ARE_UNIQUE,
                "There are two or more entity types of the same name: " + self.getName(),
                Severity.WARNING,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

### 5. Recursive/Cyclic Detection

```java
private static final String INHERITANCE_IS_NOT_RECURSIVE = "InheritanceIsNotRecursive";

@Constraint(name = INHERITANCE_IS_NOT_RECURSIVE, message = "Entity type inheritance is recursive")
public ValidationRule inheritanceIsNotRecursive() {
    return (element, context) -> {
        EntityType self = (EntityType) element;
        if (self.getAllSuperEntityTypes().contains(self)) {
            return ValidationResult.fail(
                INHERITANCE_IS_NOT_RECURSIVE,
                "Entity type " + self.getName() + " is recursive",
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

### 6. Conditional Validation with @Satisfies

```java
private static final String ABSTRACT_ENTITY_TYPE_SUPER_ENTITY_TYPES_ARE_ABSTRACT = "AbstractEntityTypeSuperEntityTypesAreAbstract";
private static final String INHERITANCE_IS_NOT_RECURSIVE = "InheritanceIsNotRecursive";

@Constraint(name = ABSTRACT_ENTITY_TYPE_SUPER_ENTITY_TYPES_ARE_ABSTRACT, message = "Abstract entity cannot have non-abstract super")
@Satisfies(INHERITANCE_IS_NOT_RECURSIVE)
public ValidationRule abstractEntityTypeSuperEntityTypesAreAbstract() {
    return (element, context) -> {
        EntityType self = (EntityType) element;
        if (!self.isAbstract()) {
            return ValidationResult.pass();
        }

        boolean hasNonAbstractSuper = self.getSuperEntityTypes().stream()
            .anyMatch(s -> !s.isAbstract());

        if (hasNonAbstractSuper) {
            return ValidationResult.fail(
                ABSTRACT_ENTITY_TYPE_SUPER_ENTITY_TYPES_ARE_ABSTRACT,
                "Abstract entity type: " + self.getName() + " cannot have non abstract super entity type(s).",
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

### 7. Name Conflict Detection

```java
private static final String INHERITING_ATTRIBUTES_AND_RELATIONS_OF_THE_SAME_NAME_IS_NOT_ALLOWED = "InheritingAttributesAndRelationsOfTheSameNameIsNotAllowed";

private Set<String> getInheritedAttributeNames(EntityType entityType) {
    return entityType.getAllSuperEntityTypes().stream()
        .flatMap(s -> s.getAttributes().stream())
        .map(a -> a.getName().toLowerCase())
        .collect(Collectors.toSet());
}

@Constraint(name = INHERITING_ATTRIBUTES_AND_RELATIONS_OF_THE_SAME_NAME_IS_NOT_ALLOWED, message = "Name conflict")
public ValidationRule inheritingAttributesAndRelationsOfTheSameNameIsNotAllowed() {
    return (element, context) -> {
        EntityType self = (EntityType) element;
        Set<String> attrNames = getInheritedAttributeNames(self);
        Set<String> relNames = getInheritedRelationNames(self);

        boolean hasConflict = attrNames.stream().anyMatch(relNames::contains);

        if (hasConflict) {
            return ValidationResult.fail(
                INHERITING_ATTRIBUTES_AND_RELATIONS_OF_THE_SAME_NAME_IS_NOT_ALLOWED,
                "Entity type: " + self.getName() + " has inherited relations and attributes of the same name.",
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

### 8. Numeric Range Validation

```java
private static final String CARDINALITY_UPPER_IS_AT_LEAST_ONE = "CardinalityUpperIsAtLeastOne";
private static final String CARDINALITY_UPPER_IS_GREATER_THAN_OR_EQUAL_TO_LOWER = "CardinalityUpperIsGreaterThanOrEqualToLower";

@Constraint(name = CARDINALITY_UPPER_IS_AT_LEAST_ONE, message = "Upper must be at least 1 or -1")
public ValidationRule cardinalityUpperIsAtLeastOne() {
    return (element, context) -> {
        Cardinality self = (Cardinality) element;
        if (self.getUpper() <= 0 && self.getUpper() != -1) {
            return ValidationResult.fail(
                CARDINALITY_UPPER_IS_AT_LEAST_ONE,
                "Upper must be at least 1 or -1 (unbounded)",
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}

@Constraint(name = CARDINALITY_UPPER_IS_GREATER_THAN_OR_EQUAL_TO_LOWER, message = "Upper must be >= lower")
public ValidationRule cardinalityUpperIsGreaterThanOrEqualToLower() {
    return (element, context) -> {
        Cardinality self = (Cardinality) element;
        if (self.getUpper() != -1 && self.getUpper() < self.getLower()) {
            return ValidationResult.fail(
                CARDINALITY_UPPER_IS_GREATER_THAN_OR_EQUAL_TO_LOWER,
                "Upper (" + self.getUpper() + ") must be >= lower (" + self.getLower() + ")",
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

### 9. Scale/Precision Validation

```java
private static final String SCALE_IS_LOWER_THAN_PRECISION = "ScaleIsLowerThanPrecision";

@Constraint(name = SCALE_IS_LOWER_THAN_PRECISION, message = "Scale must be lower than precision")
public ValidationRule scaleIsLowerThanPrecision() {
    return (element, context) -> {
        NumericType self = (NumericType) element;
        if (self.getScale() >= self.getPrecision()) {
            return ValidationResult.fail(
                SCALE_IS_LOWER_THAN_PRECISION,
                "Numeric type " + self.getName() + " scale (" + self.getScale() + 
                ") must be lower than precision (" + self.getPrecision() + ")",
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```

## Writing Tests

### Unit Test Structure

Location: `model-test/src/test/java/hu/blackbelt/judo/meta/psm/`

```java
package hu.blackbelt.judo.meta.psm;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.validation.PsmJavaValidator;

class YourValidationsTest {

    private PsmModel psmModel;
    private PsmJavaValidator validator;

    @BeforeEach
    void setUp() {
        psmModel = PsmModel.buildPsmModel()
            .uri(URI.createURI("test:test"))
            .build();
        validator = new PsmJavaValidator();
    }

    @Test
    @DisplayName("Test constraint fails when condition is violated")
    void testConstraintViolation() {
        // Create invalid model element
        EntityType entity = newEntityTypeBuilder()
            .withName("TestEntity")
            .build();
        
        psmModel.addContent(entity);
        
        // Validate
        List<ValidationResult> results = validator.validate(psmModel);
        
        // Assert specific constraint failed
        assertTrue(results.stream()
            .anyMatch(r -> r.getName().equals("YourConstraintName") 
                && r.getSeverity() == Severity.ERROR));
    }

    @Test
    @DisplayName("Test constraint passes when condition is met")
    void testConstraintPasses() {
        // Create valid model element
        EntityType entity = newEntityTypeBuilder()
            .withName("ValidEntity")
            .withValidProperty(validValue)
            .build();
        
        psmModel.addContent(entity);
        
        // Validate
        List<ValidationResult> results = validator.validate(psmModel);
        
        // Assert constraint passed
        assertTrue(results.stream()
            .noneMatch(r -> r.getName().equals("YourConstraintName") 
                && !r.isValid()));
    }
}
```

### Parameterized Test for EVL/Java Consistency

```java
@ParameterizedTest(name = "{0}")
@MethodSource("constraintProvider")
@DisplayName("EVL and Java validations produce consistent results")
void testEvlJavaConsistency(String constraintName, String evlFile) throws Exception {
    // Load test model
    PsmModel model = loadTestModel();
    
    // Run EVL validation
    List<UnsatisfiedConstraint> evlResults = runEvlValidation(model, evlFile);
    
    // Run Java validation
    List<ValidationResult> javaResults = validator.validate(model);
    
    // Compare results for this constraint
    long evlViolations = evlResults.stream()
        .filter(c -> c.getConstraint().getName().equals(constraintName))
        .count();
    
    long javaViolations = javaResults.stream()
        .filter(r -> r.getName().equals(constraintName) && !r.isValid())
        .count();
    
    assertEquals(evlViolations, javaViolations,
        "Constraint " + constraintName + " should have same violation count");
}
```

## File Organization

Validation files are organized by domain:

```
model/src/main/java/hu/blackbelt/judo/meta/psm/validation/rules/
├── accesspoint/
│   └── ActorTypeValidations.java
├── data/
│   ├── AttributeValidations.java
│   ├── CardinalityValidations.java
│   ├── EntityTypeValidations.java
│   └── RelationValidations.java
├── derived/
│   ├── DataPropertyValidations.java
│   └── NavigationPropertyValidations.java
├── measure/
│   ├── MeasureValidations.java
│   └── UnitValidations.java
├── namespace/
│   ├── ModelValidations.java
│   └── NamedElementValidations.java
├── service/
│   ├── TransferObjectTypeValidations.java
│   └── BoundOperationValidations.java
└── type/
    ├── NumericValidations.java
    ├── StringValidations.java
    └── EnumerationTypeValidations.java
```

## ValidationContext API

The `context` parameter provides:

```java
// Get all instances of a type in the model
Collection<T> instances = context.getAllInstances(EntityType.class);

// Access the model resource
Resource resource = context.getResource();

// Check if an element was already validated
boolean validated = context.isValidated(element, "ConstraintName");
```

## Best Practices

1. **Use Constants for Names**: Always define constraint/critique names as `private static final String` constants
   - Use UPPER_SNAKE_CASE for constant names
   - Place constants at the top of the class, after the class declaration
   - Use the same constant in `@Constraint(name = ...)`, `@Satisfies(...)`, and `ValidationResult.fail(...)`

2. **Constraint Naming**: Use PascalCase for the string value, be descriptive (e.g., `InheritanceIsNotRecursive`)

3. **Message Format**: Include element name and context in error messages

4. **Use @Satisfies**: Chain dependent constraints to avoid null pointer errors

5. **Helper Methods**: Extract reusable logic into private helper methods

6. **Early Return**: Return `ValidationResult.pass()` early when preconditions aren't met

7. **Severity Levels**:
   - `@Constraint` = `Severity.ERROR` (must fix)
   - `@Critique` = `Severity.WARNING` (recommendation)

8. **Case-Insensitive Comparison**: Use `.equalsIgnoreCase()` for name comparisons

9. **External References**: When using `@Satisfies` to reference constraints from other classes, define the constant locally with a comment indicating it's external

## Common Imports

```java
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Critique;
import hu.blackbelt.judo.zeta.annotation.Satisfies;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import hu.blackbelt.judo.zeta.validation.core.Severity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
```

## Migration from EVL

When converting EVL constraints to Java:

| EVL | Java |
|-----|------|
| `context EntityType` | `@ValidationContext(EntityType.class)` |
| `constraint Name { ... }` | `private static final String NAME = "Name";` + `@Constraint(name = NAME, message = "...")` |
| `critique Name { ... }` | `private static final String NAME = "Name";` + `@Critique(name = NAME, message = "...")` |
| `guard: self.isAbstract()` | Check condition at start and return pass if not met |
| `check: ...` | Return `ValidationResult.fail(CONSTANT_NAME, ...)` if check fails |
| `message: "..."` | Include in `ValidationResult.fail()` call |
| `satisfies Name` | `private static final String NAME = "Name";` + `@Satisfies(NAME)` |
| `self.allInstances()` | `context.getAllInstances(Type.class)` |

## References

- [Zeta Validation Getting Started](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/getting-started.md)
- [Annotation Reference](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/reference/annotations.md)
- [Simple Validation Examples](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/examples/simple-validations.md)
- [EVL Comparison](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/evl-comparison/overview.md)
