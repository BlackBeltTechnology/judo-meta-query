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

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.epsilon.common.util.UriUtil;

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import hu.blackbelt.judo.meta.query.runtime.QueryModel;

public class QueryEpsilonValidator {

    public static void validateQuery(Log log,
                                   QueryModel queryModel,
                                   URI scriptRoot) throws ScriptExecutionException, URISyntaxException
    {
        validateQuery(log, queryModel, scriptRoot, emptyList(), emptyList());
    }

    public static void validateQuery(Log log,
                                   QueryModel queryModel,
                                   URI scriptRoot,
                                   Collection<String> expectedErrors,
                                   Collection<String> expectedWarnings) throws ScriptExecutionException, URISyntaxException
    {
        ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(queryModel.getResourceSet())
                .metaModels(emptyList())
                .modelContexts(Arrays.asList(
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("QUERY")
                                .validateModel(false)
                                .resource(queryModel.getResource())
                                .build()))
                .injectContexts(singletonMap("queryUtils", new QueryUtils()))
                .build();

         try {
                // run the model / metadata loading
                executionContext.load();

                // Transformation script
                executionContext.executeProgram(
                        evlExecutionContextBuilder()
                                .source(UriUtil.resolve("query.evl", scriptRoot))
                                .expectedErrors(expectedErrors)
                                .expectedWarnings(expectedWarnings)
                                .build());

            } finally {
                executionContext.commit();
                try {
                    executionContext.close();
                } catch (Exception e) {}
            }
    }

    public static URI calculateQueryValidationScriptURI() throws URISyntaxException {
        URI queryRoot = QueryModel.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        if (queryRoot.toString().endsWith(".jar")) {
            queryRoot = new URI("jar:" + queryRoot.toString() + "!/validations/");
        } else if (queryRoot.toString().startsWith("jar:bundle:")) {
            // bundle://37.0:0/validations/
            // jar:bundle://37.0:0/!/validations/query.evl
            queryRoot = new URI(queryRoot.toString().substring(4, queryRoot.toString().indexOf("!")) + "validations/");
        } else {
            queryRoot = new URI(queryRoot.toString() + "/validations/");
        }
        return queryRoot;

    }

}
