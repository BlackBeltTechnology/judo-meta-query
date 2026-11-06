package hu.blackbelt.judo.meta.query.runtime;

/*-
 * #%L
 * Judo :: Query :: Model
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

import hu.blackbelt.judo.meta.query.AbstractQueryValidationTest;
import hu.blackbelt.judo.meta.query.ValidatorType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Parameterized validation tests for Query model.
 *
 * <p>Each test runs twice - once with EVL validator and once with Java (Zeta) validator.
 * Both validators must produce identical results for tests to pass.</p>
 */
@Slf4j
public class QueryValidationTest extends AbstractQueryValidationTest {

    /**
     * Test that an empty model validates successfully.
     * This is the baseline test - both validators should accept a minimal valid model.
     */
    @ParameterizedTest(name = "{0}")
    @EnumSource(ValidatorType.class)
    void testEmptyModelValidates(ValidatorType validatorType) throws Exception {
        // An empty Query model should be valid
        runValidationExpectingNoIssues(validatorType);
    }

    // TODO: Add more validation tests as EVL rules are implemented (JNG-4275)
    // Example test structure:
    //
    // @ParameterizedTest(name = "{0}")
    // @EnumSource(ValidatorType.class)
    // void testSelectMustHaveFrom(ValidatorType validatorType) throws Exception {
    //     // Create an invalid Select without 'from' reference
    //     Select select = queryModel.getQueryFactory().createSelect();
    //     queryModel.getResource().getContents().add(select);
    //
    //     // Expect the SelectMustHaveFrom constraint to fail
    //     runValidation(
    //         validatorType,
    //         List.of(QueryValidationConstants.SELECT_MUST_HAVE_FROM),
    //         Collections.emptyList()
    //     );
    // }
}
