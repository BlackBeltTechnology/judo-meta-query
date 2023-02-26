package hu.blackbelt.judo.meta.query.osgi.itest;

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

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.query.runtime.QueryModel;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;

import static hu.blackbelt.judo.meta.query.osgi.itest.KarafFeatureProvider.karafConfig;
import static hu.blackbelt.judo.meta.query.osgi.itest.KarafFeatureProvider.testTargetDir;
import static hu.blackbelt.judo.meta.query.runtime.QueryEpsilonValidator.calculateQueryValidationScriptURI;
import static hu.blackbelt.judo.meta.query.runtime.QueryEpsilonValidator.validateQuery;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

@Ignore
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@Slf4j
public class QueryModelLoadITest {

    private static final String DEMO = "northwind-query";

    @Inject
    protected BundleTrackerManager bundleTrackerManager;

    @Inject
    BundleContext bundleContext;

    @Inject
    QueryModel queryModel;

    @Configuration
    public Option[] config() throws FileNotFoundException, MalformedURLException {

        return combine(karafConfig(this.getClass()),
                mavenBundle(maven()
                        .groupId("hu.blackbelt.judo.meta")
                        .artifactId("hu.blackbelt.judo.meta.query.osgi")
                        .versionAsInProject()),
                getProvisonModelBundle());
    }

    public Option getProvisonModelBundle() throws FileNotFoundException {
        return provision(
                getQueryModelBundle()
        );
    }

    private InputStream getQueryModelBundle() throws FileNotFoundException {
        return bundle()
                .add( "model/" + queryModel.getName() + "-query.model",
                        new FileInputStream(new File(testTargetDir(getClass()).getAbsolutePath(),  "northwind-query.model")))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, queryModel.getName() + "-query" )
                //set( Constants.IMPORT_PACKAGE, "meta/psm;version=\"" + getConfiguration(META_PSM_IMPORT_RANGE) +"\"")
                .set( "Query-Models", "name=" + queryModel.getName() + ";file=model/" + queryModel.getName() + "-query.model")
                .build( withBnd());
    }

    @Test
    public void testModelValidation() throws Exception {
        try (Log bufferedLog = new BufferedSlf4jLogger(log)) {
            validateQuery(bufferedLog, queryModel, calculateQueryValidationScriptURI());
        }
    }
}
