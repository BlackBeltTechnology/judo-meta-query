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

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.query.*;
import hu.blackbelt.judo.meta.query.runtime.QueryEpsilonValidator;
import hu.blackbelt.judo.meta.query.runtime.QueryModel;
import hu.blackbelt.judo.meta.query.runtime.QueryUtils;
import hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport;
import hu.blackbelt.judo.meta.query.util.builder.QueryBuilders;
import hu.blackbelt.judo.meta.query.validation.QueryValidator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.epsilon.common.util.UriUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport.queryModelResourceSupportBuilder;
import static java.util.Arrays.asList;

/**
 * Performance benchmark test comparing EVL vs Java validation.
 *
 * <p>This test generates a large model with many Select/Join elements and measures
 * the validation time for both EVL and Java validators.</p>
 */
@Slf4j
public class QueryValidationPerformanceTest {

    private static final String MODEL_NAME = "PerformanceTestModel";
    private static final int SELECT_COUNT = 200;
    private static final int JOINS_PER_SELECT = 5;
    private static final int FEATURES_PER_SELECT = 3;
    private static final int WARMUP_ITERATIONS = 1;
    private static final int BENCHMARK_ITERATIONS = 3;

    private QueryModelResourceSupport queryModelSupport;
    private QueryModel queryModel;

    @BeforeEach
    void setUp() {
        queryModelSupport = queryModelResourceSupportBuilder()
                .uri(URI.createFileURI("urn:Query.performance-test"))
                .build();

        queryModel = QueryModel.buildQueryModel()
                .queryModelResourceSupport(queryModelSupport)
                .name(MODEL_NAME)
                .build();
    }

    @Test
    void benchmarkEvlVsJavaValidation() throws Exception {
        log.info("=".repeat(80));
        log.info("Query Validation Performance Benchmark");
        log.info("=".repeat(80));
        log.info("Configuration:");
        log.info("  - Select count: {}", SELECT_COUNT);
        log.info("  - Joins per select: {}", JOINS_PER_SELECT);
        log.info("  - Features per select: {}", FEATURES_PER_SELECT);
        log.info("  - Warmup iterations: {}", WARMUP_ITERATIONS);
        log.info("  - Benchmark iterations: {}", BENCHMARK_ITERATIONS);
        log.info("-".repeat(80));

        // Generate the large model
        log.info("Generating large model...");
        long modelGenStart = System.currentTimeMillis();
        generateLargeModel();
        long modelGenTime = System.currentTimeMillis() - modelGenStart;
        log.info("Model generation completed in {} ms", modelGenTime);

        int elementCount = countModelElements();
        log.info("Total model elements: {}", elementCount);
        log.info("-".repeat(80));

        // Warmup phase
        log.info("Warmup phase ({} iterations)...", WARMUP_ITERATIONS);
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runEvlValidation();
            runJavaValidation(false);
            runJavaValidation(true);
        }
        log.info("Warmup completed.");
        log.info("-".repeat(80));

        // Benchmark EVL validation
        log.info("Benchmarking EVL validation ({} iterations)...", BENCHMARK_ITERATIONS);
        List<Long> evlTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runEvlValidation();
            long elapsed = System.currentTimeMillis() - start;
            evlTimes.add(elapsed);
            log.info("  EVL iteration {}: {} ms", i + 1, elapsed);
        }

        // Benchmark Java validation (sequential)
        log.info("Benchmarking Java validation - Sequential ({} iterations)...", BENCHMARK_ITERATIONS);
        List<Long> javaSeqTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runJavaValidation(false);
            long elapsed = System.currentTimeMillis() - start;
            javaSeqTimes.add(elapsed);
            log.info("  Java Sequential iteration {}: {} ms", i + 1, elapsed);
        }

        // Benchmark Java validation (parallel)
        log.info("Benchmarking Java validation - Parallel ({} iterations)...", BENCHMARK_ITERATIONS);
        List<Long> javaParTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runJavaValidation(true);
            long elapsed = System.currentTimeMillis() - start;
            javaParTimes.add(elapsed);
            log.info("  Java Parallel iteration {}: {} ms", i + 1, elapsed);
        }

        // Calculate statistics
        log.info("=".repeat(80));
        log.info("RESULTS");
        log.info("=".repeat(80));

        double evlAvg = calculateAverage(evlTimes);
        double evlMin = Collections.min(evlTimes);
        double evlMax = Collections.max(evlTimes);

        double javaSeqAvg = calculateAverage(javaSeqTimes);
        double javaSeqMin = Collections.min(javaSeqTimes);
        double javaSeqMax = Collections.max(javaSeqTimes);

        double javaParAvg = calculateAverage(javaParTimes);
        double javaParMin = Collections.min(javaParTimes);
        double javaParMax = Collections.max(javaParTimes);

        log.info("");
        log.info("EVL Validation:");
        log.info("  Average: {} ms", String.format("%.2f", evlAvg));
        log.info("  Min: {} ms, Max: {} ms", (long) evlMin, (long) evlMax);

        log.info("");
        log.info("Java Validation (Sequential):");
        log.info("  Average: {} ms", String.format("%.2f", javaSeqAvg));
        log.info("  Min: {} ms, Max: {} ms", (long) javaSeqMin, (long) javaSeqMax);
        if (evlAvg > 0) {
            log.info("  Speedup vs EVL: {}x", String.format("%.2f", evlAvg / javaSeqAvg));
        }

        log.info("");
        log.info("Java Validation (Parallel):");
        log.info("  Average: {} ms", String.format("%.2f", javaParAvg));
        log.info("  Min: {} ms, Max: {} ms", (long) javaParMin, (long) javaParMax);
        if (evlAvg > 0) {
            log.info("  Speedup vs EVL: {}x", String.format("%.2f", evlAvg / javaParAvg));
        }
        if (javaSeqAvg > 0) {
            log.info("  Speedup vs Java Sequential: {}x", String.format("%.2f", javaSeqAvg / javaParAvg));
        }

        log.info("");
        log.info("=".repeat(80));
        log.info("Model Statistics:");
        log.info("  Total elements validated: {}", elementCount);
        if (evlAvg > 0) {
            log.info("  Elements per millisecond (EVL): {}", String.format("%.2f", elementCount / evlAvg));
        }
        if (javaSeqAvg > 0) {
            log.info("  Elements per millisecond (Java Seq): {}", String.format("%.2f", elementCount / javaSeqAvg));
        }
        if (javaParAvg > 0) {
            log.info("  Elements per millisecond (Java Par): {}", String.format("%.2f", elementCount / javaParAvg));
        }
        log.info("=".repeat(80));
    }

    private void generateLargeModel() {
        // Create multiple Select elements with Targets and SubSelectJoins
        for (int i = 0; i < SELECT_COUNT; i++) {
            Select select = QueryBuilders.newSelectBuilder()
                    .withAlias("s" + i)
                    .build();

            // Create Target for the Select
            Target target = QueryBuilders.newTargetBuilder()
                    .withIndex(i)
                    .build();
            select.getTargets().add(target);
            select.setMainTarget(target);

            // Create SubSelectJoins for this Select
            for (int j = 0; j < JOINS_PER_SELECT; j++) {
                SubSelectJoin join = QueryBuilders.newSubSelectJoinBuilder()
                        .withAlias("j" + i + "_" + j)
                        .build();
                select.getJoins().add(join);
            }

            // Create Filter elements for this Select
            for (int f = 0; f < FEATURES_PER_SELECT; f++) {
                Filter filter = QueryBuilders.newFilterBuilder()
                        .withAlias("f" + i + "_" + f)
                        .build();
                select.getFilters().add(filter);
            }

            // Add the select to the model
            queryModelSupport.addContent(select);
        }
    }

    private int countModelElements() {
        final int[] count = {0};
        queryModel.getResourceSet().getResources().forEach(resource -> {
            resource.getAllContents().forEachRemaining(e -> count[0]++);
        });
        return count[0];
    }

    private void runEvlValidation() throws Exception {
        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            ExecutionContext executionContext = executionContextBuilder()
                    .log(bufferedLogger)
                    .resourceSet(queryModel.getResourceSet())
                    .metaModels(emptyList())
                    .modelContexts(asList(
                            wrappedEmfModelContextBuilder()
                                    .log(bufferedLogger)
                                    .name("QUERY")
                                    .resource(queryModel.getResource())
                                    .validateModel(false)
                                    .useCache(false)
                                    .build()
                    ))
                    .injectContexts(singletonMap("queryUtils", new QueryUtils()))
                    .build();

            try {
                executionContext.load();
                executionContext.executeProgram(
                        evlExecutionContextBuilder()
                                .source(UriUtil.resolve("query.evl",
                                        QueryEpsilonValidator.calculateQueryValidationScriptURI()))
                                .expectedErrors(emptyList())
                                .expectedWarnings(emptyList())
                                .parallel(false)
                                .build()
                );
            } catch (Exception e) {
                // Validation errors are expected - we're just measuring time
            } finally {
                executionContext.commit();
                try {
                    executionContext.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    private void runJavaValidation(boolean parallel) throws Exception {
        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            try {
                QueryValidator.validateQuery(
                        bufferedLogger,
                        queryModel,
                        emptyList(),
                        emptyList(),
                        parallel
                );
            } catch (Exception e) {
                // Validation errors are expected - we're just measuring time
            }
        }
    }

    private double calculateAverage(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }
}
