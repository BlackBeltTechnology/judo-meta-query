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
