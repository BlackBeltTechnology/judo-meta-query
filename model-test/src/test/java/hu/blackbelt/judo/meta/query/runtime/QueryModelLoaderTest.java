package hu.blackbelt.judo.meta.query.runtime;

import hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static hu.blackbelt.judo.meta.query.runtime.QueryModel.LoadArguments.queryLoadArgumentsBuilder;

@Disabled
public class QueryModelLoaderTest {

    static Logger log = LoggerFactory.getLogger(QueryModelLoaderTest.class);
	
    @Test
    @DisplayName("Load Query Model")
    void loadQueryModel() throws IOException, QueryModel.QueryValidationException {
        ResourceSet queryResourceSet = QueryModelResourceSupport.createQueryResourceSet();

        QueryModel queryModel = QueryModel.loadQueryModel(queryLoadArgumentsBuilder()
                .resourceSet(queryResourceSet)
                .uri(URI.createFileURI(new File("target/test-classes/model/northwind-query.model").getAbsolutePath()))
                .name("test")
                .build());

        for (Iterator<EObject> i = queryModel.getResourceSet().getResource(queryModel.getUri(), false).getAllContents(); i.hasNext(); ) {
            log.info(i.next().toString());
        }
    }
}
