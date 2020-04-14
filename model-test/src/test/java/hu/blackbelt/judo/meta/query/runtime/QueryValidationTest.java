package hu.blackbelt.judo.meta.query.runtime;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Collection;

import static hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport.queryModelResourceSupportBuilder;


public class QueryValidationTest {

    QueryModelResourceSupport queryModelSupport;
    
    private static final Logger logger = LoggerFactory.getLogger(QueryValidationTest.class);
    private final String createdSourceModelName = "urn:Query.model";

    private Log log = new Slf4jLog();
    private QueryModel queryModel;
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


    private void runEpsilon (Collection<String> expectedErrors, Collection<String> expectedWarnings) throws Exception {
        try {
            QueryEpsilonValidator.validateQuery(log,
            		queryModel,
                    QueryEpsilonValidator.calculateQueryValidationScriptURI(),
                    expectedErrors,
                    expectedWarnings);
        } catch (EvlScriptExecutionException ex) {
            logger.error("EVL failed", ex);
            logger.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            logger.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            logger.error("\u001B[31m - errors not found: {}\u001B[0m", ex.getErrorsNotFound());
            logger.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            logger.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            logger.error("\u001B[33m - warnings not found: {}\u001B[0m", ex.getWarningsNotFound());
            throw ex;
        }
    }
}
