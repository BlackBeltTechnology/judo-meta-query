package hu.blackbelt.judo.meta.query.runtime;

import hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport.queryModelResourceSupportBuilder;

class QueryExecutionContextTest {

    @Test
    @DisplayName("Create Query model with builder pattern")
    void testQueryReflectiveCreated() throws Exception {

        String createdSourceModelName = "urn:query.judo-meta-query";

        QueryModelResourceSupport queryModelSupport = queryModelResourceSupportBuilder()
                .uri(URI.createURI(createdSourceModelName))
                .build();

        // Build model here
    }
}
