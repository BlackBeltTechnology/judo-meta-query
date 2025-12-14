package hu.blackbelt.judo.meta.query;

/*-
 * #%L
 * Judo :: Query :: Model :: Test
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.query.runtime.QueryEpsilonValidator;
import hu.blackbelt.judo.meta.query.runtime.QueryModel;
import hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport;
import hu.blackbelt.judo.meta.query.validation.QueryValidator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;
import java.util.Collections;

import static hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport.queryModelResourceSupportBuilder;

/**
 * Base class for Query model validation tests.
 *
 * <p>Provides infrastructure for running validation tests with both EVL and Java validators.
 * Test classes should extend this class and use {@code @ParameterizedTest} with
 * {@code @EnumSource(ValidatorType.class)} to run tests against both validators.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @ParameterizedTest
 * @EnumSource(ValidatorType.class)
 * void testSelectMustHaveFrom(ValidatorType validatorType) throws Exception {
 *     // Setup model with invalid data
 *     ...
 *     // Run validation expecting errors
 *     runValidation(validatorType, List.of("SelectMustHaveFrom"), Collections.emptyList());
 * }
 * }
 * </pre>
 *
 * <p><b>NOTE:</b> Java validation requires judo-zeta to be available. Until then,
 * Java tests are skipped with an assumption failure.</p>
 */
@Slf4j
public abstract class AbstractQueryValidationTest {

    protected QueryModelResourceSupport queryModelSupport;
    protected QueryModel queryModel;

    private final String createdSourceModelName = "urn:Query.model";

    @BeforeEach
    void setUp() {
        queryModelSupport = queryModelResourceSupportBuilder()
                .uri(URI.createFileURI(createdSourceModelName))
                .build();

        queryModel = QueryModel.buildQueryModel()
                .queryModelResourceSupport(queryModelSupport)
                .name("test")
                .build();
    }

    /**
     * Run validation using the specified validator type.
     *
     * @param validatorType which validator to use (EVL or JAVA)
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @throws Exception if validation fails unexpectedly
     */
    protected void runValidation(
            ValidatorType validatorType,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws Exception {
        log.info("Running {} validation with expected errors: {}, expected warnings: {}",
                validatorType, expectedErrors, expectedWarnings);

        switch (validatorType) {
            case EVL:
                runEvlValidation(expectedErrors, expectedWarnings);
                break;
            case JAVA:
                runJavaValidation(expectedErrors, expectedWarnings);
                break;
            default:
                throw new IllegalArgumentException("Unknown validator type: " + validatorType);
        }
    }

    /**
     * Run validation expecting no errors or warnings.
     *
     * @param validatorType which validator to use (EVL or JAVA)
     * @throws Exception if validation fails
     */
    protected void runValidationExpectingNoIssues(ValidatorType validatorType) throws Exception {
        runValidation(validatorType, Collections.emptyList(), Collections.emptyList());
    }

    private void runEvlValidation(
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws Exception {
        try (BufferedSlf4jLogger bufferedLog = new BufferedSlf4jLogger(log)) {
            QueryEpsilonValidator.validateQuery(
                    bufferedLog,
                    queryModel,
                    QueryEpsilonValidator.calculateQueryValidationScriptURI(),
                    expectedErrors,
                    expectedWarnings
            );
        } catch (EvlScriptExecutionException ex) {
            log.error("EVL validation failed", ex);
            log.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            log.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            log.error("\u001B[31m - errors not found: {}\u001B[0m", ex.getErrorsNotFound());
            log.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            log.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            log.error("\u001B[33m - warnings not found: {}\u001B[0m", ex.getWarningsNotFound());
            throw ex;
        }
    }

    private void runJavaValidation(
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws Exception {
        try {
            QueryValidator.validateQuery(
                    log,
                    queryModel,
                    expectedErrors,
                    expectedWarnings
            );
        } catch (QueryValidator.QueryValidationException ex) {
            log.error("Java validation failed");
            log.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            log.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            throw ex;
        }
    }
}
