package hu.blackbelt.judo.meta.query.validation;

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

import hu.blackbelt.judo.meta.query.runtime.QueryModel;
import hu.blackbelt.judo.meta.query.runtime.QueryUtils;
import hu.blackbelt.judo.zeta.common.ExtensionMethodRegistry;
import hu.blackbelt.judo.zeta.validation.core.Severity;
import hu.blackbelt.judo.zeta.validation.core.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationExecutor;
import hu.blackbelt.judo.zeta.validation.core.ValidationRegistry;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Entry point for Java-based Query model validation.
 *
 * <p>This validator provides a native Java alternative to EVL (Epsilon Validation Language)
 * validation with better IDE integration, debugging support, and performance.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * QueryValidator.validateQuery(log, queryModel);
 * }
 * </pre>
 */
public class QueryValidator {

    /**
     * Exception thrown when Query validation fails.
     */
    public static class QueryValidationException extends Exception {
        private final QueryModel queryModel;

        public QueryValidationException(QueryModel queryModel) {
            super("Query model validation failed");
            this.queryModel = queryModel;
        }

        public QueryModel getQueryModel() {
            return queryModel;
        }
    }

    /**
     * Validate Query model using Java validation rules.
     *
     * @param log the logger
     * @param queryModel the model to validate
     * @throws QueryValidationException if validation fails
     */
    public static void validateQuery(Logger log, QueryModel queryModel)
            throws QueryValidationException {
        validateQuery(log, queryModel, null, null, false);
    }

    /**
     * Validate Query model with expected errors and warnings (for testing).
     *
     * @param log the logger
     * @param queryModel the model to validate
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @throws QueryValidationException if validation fails
     */
    public static void validateQuery(
            Logger log,
            QueryModel queryModel,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws QueryValidationException {
        validateQuery(log, queryModel, expectedErrors, expectedWarnings, false);
    }

    /**
     * Validate Query model with all options.
     *
     * @param log the logger
     * @param queryModel the model to validate
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @param parallel use parallel execution
     * @throws QueryValidationException if validation fails
     */
    public static void validateQuery(
            Logger log,
            QueryModel queryModel,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings,
            boolean parallel
    ) throws QueryValidationException {
        log.info("Starting Java-based Query validation...");

        // Create validation infrastructure
        ValidationRegistry registry = new ValidationRegistry();
        ExtensionMethodRegistry extensionRegistry = new ExtensionMethodRegistry();

        // Create QueryUtils instance for model traversal (implements ModelProvider)
        QueryUtils queryUtils = new QueryUtils();

        // Create validation context
        ValidationContext context = new ValidationContext(
                queryUtils,
                queryModel.getResourceSet(),
                extensionRegistry
        );

        // Register validation rule classes
        // NOTE: No validation rules exist yet (EVL rules are TODO - JNG-4275)
        // When rules are implemented, register them here:
        // registry.register(SelectValidations.class);
        // registry.register(JoinValidations.class);
        // etc.

        // Set the registry on the context for satisfies() evaluation
        context.setValidationRegistry(registry);

        // Create executor
        ValidationExecutor executor = new ValidationExecutor(
                registry,
                context,
                parallel
        );

        try {
            // Collect all elements to validate
            List<EObject> allElements = new ArrayList<>();
            queryModel.getResourceSet().getResources().forEach(resource -> {
                Iterator<EObject> it = resource.getAllContents();
                while (it.hasNext()) {
                    allElements.add(it.next());
                }
            });

            log.info("Validating {} elements...", allElements.size());

            // Execute validation
            List<ValidationResult> failures = executor.validate(allElements);

            // Separate errors and warnings
            List<ValidationResult> errors = failures.stream()
                    .filter(r -> r.getSeverity() == Severity.ERROR)
                    .collect(Collectors.toList());

            List<ValidationResult> warnings = failures.stream()
                    .filter(r -> r.getSeverity() == Severity.WARNING)
                    .collect(Collectors.toList());

            log.info("Validation complete: {} errors, {} warnings",
                    errors.size(), warnings.size());

            // Check against expected results
            boolean hasUnexpected = false;
            if (expectedErrors != null || expectedWarnings != null) {
                hasUnexpected = checkExpectedResults(
                        log, errors, warnings, expectedErrors, expectedWarnings);
            }

            // Throw exception if there are unexpected failures
            if (!errors.isEmpty()) {
                logFailures(log, errors, "ERRORS");
                if (expectedErrors == null || hasUnexpected) {
                    throw new QueryValidationException(queryModel);
                }
            }

            if (!warnings.isEmpty()) {
                logFailures(log, warnings, "WARNINGS");
            }
        } finally {
            executor.shutdown();
        }
    }

    private static boolean checkExpectedResults(
            Logger log,
            List<ValidationResult> errors,
            List<ValidationResult> warnings,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) {
        Set<String> actualErrors = errors.stream()
                .map(ValidationResult::getConstraintName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> actualWarnings = warnings.stream()
                .map(ValidationResult::getConstraintName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> expectedErrorSet = expectedErrors != null
                ? new HashSet<>(expectedErrors)
                : Collections.emptySet();
        Set<String> expectedWarningSet = expectedWarnings != null
                ? new HashSet<>(expectedWarnings)
                : Collections.emptySet();

        // Find unexpected and missing errors
        Set<String> unexpectedErrors = new HashSet<>(actualErrors);
        unexpectedErrors.removeAll(expectedErrorSet);

        Set<String> missingErrors = new HashSet<>(expectedErrorSet);
        missingErrors.removeAll(actualErrors);

        // Only check warnings if expectedWarnings was explicitly provided
        Set<String> unexpectedWarnings = Collections.emptySet();
        Set<String> missingWarnings = Collections.emptySet();
        if (expectedWarnings != null) {
            unexpectedWarnings = new HashSet<>(actualWarnings);
            unexpectedWarnings.removeAll(expectedWarningSet);
            missingWarnings = new HashSet<>(expectedWarningSet);
            missingWarnings.removeAll(actualWarnings);
        }

        boolean hasUnexpected = !unexpectedErrors.isEmpty()
                || !missingErrors.isEmpty()
                || !unexpectedWarnings.isEmpty()
                || !missingWarnings.isEmpty();

        if (hasUnexpected) {
            log.error("Validation result mismatch:");
            log.error("  Actual errors: {}", actualErrors);
            log.error("  Expected errors: {}", expectedErrorSet);
            log.error("  Actual warnings: {}", actualWarnings);
            log.error("  Expected warnings: {}", expectedWarningSet);
            if (!unexpectedErrors.isEmpty()) {
                log.error("  Unexpected errors: {}", unexpectedErrors);
            }
            if (!missingErrors.isEmpty()) {
                log.error("  Missing errors: {}", missingErrors);
            }
            if (!unexpectedWarnings.isEmpty()) {
                log.error("  Unexpected warnings: {}", unexpectedWarnings);
            }
            if (!missingWarnings.isEmpty()) {
                log.error("  Missing warnings: {}", missingWarnings);
            }
        }

        return hasUnexpected;
    }

    private static void logFailures(
            Logger log,
            List<ValidationResult> failures,
            String label
    ) {
        log.info("{}: {}", label, failures.size());
        for (ValidationResult failure : failures) {
            log.info("  [{}] {}: {}",
                    failure.getSeverity(),
                    failure.getConstraintName(),
                    failure.getMessage());
        }
    }
}
