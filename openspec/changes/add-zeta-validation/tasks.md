# Tasks: Add Zeta Validation Framework

## Status Notes

**Implementation Status:** Complete - judo-zeta integrated and working

The Zeta Java validation framework is now fully integrated:
- judo-zeta is built and installed locally from `/Users/robson/Project/judo-ng/runtime/judo-zeta`
- All pom.xml files have Zeta dependencies enabled
- QueryValidator.java is implemented and working
- Both EVL and Java validators run in parallel via parameterized tests
- Performance test is in place

## 1. Project Configuration

- [x] 1.1 Add `judo-zeta-version` property to parent `pom.xml`
- [x] 1.2 Add judo-zeta dependencies to `model/pom.xml`
- [x] 1.3 Add judo-zeta test dependencies to `model-test/pom.xml`
- [x] 1.4 Verify build compiles with changes (`mvn clean compile`)

## 2. Validation Infrastructure (model/)

- [x] 2.1 Create package `hu.blackbelt.judo.meta.query.validation`
- [x] 2.2 Create `QueryValidationConstants.java` with placeholder for constants
- [x] 2.3 Create `QueryValidator.java` entry point using Zeta framework
- [x] 2.4 Add `getAllContents()` method to `QueryUtils` implementing `ModelProvider` interface
- [ ] 2.5 Create `rules/` subpackage structure (deferred until rules exist)

## 3. Implement Validation Rules (model/)

Based on EVL rules in `query.evl` and `query-plugin-validation.evl`:

- [x] 3.1 Analyze EVL files - found only TODO placeholder (JNG-4275)
- [ ] 3.2-3.14 Implement validation rule classes (deferred - no EVL rules to convert)

## 4. Test Infrastructure (model-test/)

- [x] 4.1 Create `ValidatorType.java` enum (EVL, JAVA)
- [x] 4.2 Create `AbstractQueryValidationTest.java` base class
- [x] 4.3 Implement `runValidation()` method with EVL and Java support
- [x] 4.4 Java validation now enabled (assumption check removed)

## 5. Convert Existing Tests to Parameterized (model-test/)

- [x] 5.1 Analyze existing `QueryValidationTest.java` test structure
- [x] 5.2 Convert tests to extend `AbstractQueryValidationTest`
- [x] 5.3 Add `@ParameterizedTest` and `@EnumSource(ValidatorType.class)` annotations
- [x] 5.4 Update test methods to call `runValidation()`
- [x] 5.5 EVL tests pass
- [x] 5.6 Java tests pass
- [x] 5.7 Both validators produce identical results (both pass with 0 errors, 0 warnings)

## 6. Performance Testing

- [x] 6.1 Create `QueryValidationPerformanceTest.java`
- [x] 6.2 Implement performance benchmark comparing EVL vs Java (sequential and parallel)
- [x] 6.3 Model generation with Select, Target, SubSelectJoin, Filter elements
- [x] 6.4 Warmup and benchmark iterations
- [x] 6.5 Statistics calculation and reporting

## 7. Documentation

- [x] 7.1 Create `docs/validation/` directory
- [x] 7.2 Create `docs/validation/README.md` with overview
- [x] 7.3 Create `docs/validation/java-validation-framework.md` referencing Zeta docs
- [x] 7.4 Create `docs/validation/query-validation-rules.md` documenting planned rules
- [x] 7.5 Update `AGENTS.md` (already has validation references)
- [x] 7.6 README.md already updated

## 8. Documentation Conversion

- [x] 8.1 Identify `.adoc` files (found `.github/CIFLOW.adoc`)
- [x] 8.2 Convert CIFLOW.adoc to Markdown
- [x] 8.3 Convert PlantUML diagrams to Mermaid format
- [x] 8.4 Delete original `.adoc` file
- [x] 8.5 No internal links to update (only adoc was CIFLOW)

## 9. Validation and Cleanup

- [x] 9.1 Run compile (`mvn clean compile` - passes)
- [x] 9.2 Run full test suite (`mvn clean install` - passes)
- [x] 9.3 Check for remaining TODOs - documented as pending JNG-4275 for EVL rules
- [x] 9.4 Review constant usages - constants file ready for rules
- [x] 9.5 Build verification complete
- [x] 9.6 Update OpenSpec tasks to reflect actual completion

## Dependencies

- **judo-zeta:** Integrated. Built locally from `/Users/robson/Project/judo-ng/runtime/judo-zeta`
- **JNG-4275:** EVL rules are placeholder only - actual rules pending

## Implementation Notes

### Key Files Created/Modified

1. **pom.xml (parent)**: Added `judo-zeta-version` property and Zeta dependencies
2. **model/pom.xml**: Added Zeta dependencies with `provided` scope
3. **model-test/pom.xml**: Added Zeta dependencies with `test` scope
4. **QueryUtils.java**: Implements `ModelProvider` interface from judo-zeta
5. **QueryValidator.java**: Entry point for Java validation using Zeta framework
6. **AbstractQueryValidationTest.java**: Base class supporting both EVL and Java validation
7. **QueryValidationPerformanceTest.java**: Performance benchmark test

### Zeta Package Structure

- `hu.blackbelt.judo.zeta.annotation` - Annotations (@Constraint, @Critique, @Guard, etc.)
- `hu.blackbelt.judo.zeta.validation.core` - Core classes (ValidationExecutor, ValidationRegistry, etc.)
- `hu.blackbelt.judo.zeta.common` - Common utilities (ModelProvider, ExtensionMethodRegistry)

### Next Steps

1. Implement EVL validation rules (JNG-4275)
2. Create corresponding Java validation rules using Zeta annotations
3. Add constants to `QueryValidationConstants.java` for rule names
4. Run performance benchmarks to compare EVL vs Java validation speed
