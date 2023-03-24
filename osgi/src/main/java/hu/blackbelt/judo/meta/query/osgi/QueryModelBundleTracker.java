package hu.blackbelt.judo.meta.query.osgi;

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
import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.VersionRange;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Component(immediate = true)
@Slf4j
public class QueryModelBundleTracker {

    public static final String QUERY_MODELS = "Query-Models";

    @Reference
    BundleTrackerManager bundleTrackerManager;

    Map<String, ServiceRegistration<QueryModel>> queryModelRegistrations = new ConcurrentHashMap<>();

    Map<String, QueryModel> queryModels = new HashMap<>();

    @Activate
    public void activate(final ComponentContext componentContext) {
        bundleTrackerManager.registerBundleCallback(this.getClass().getName(),
                new QueryRegisterCallback(componentContext.getBundleContext()),
                new QueryUnregisterCallback(),
                new QueryBundlePredicate());
    }

    @Deactivate
    public void deactivate(final ComponentContext componentContext) {
        bundleTrackerManager.unregisterBundleCallback(this.getClass().getName());
    }

    private static class QueryBundlePredicate implements Predicate<Bundle> {
        @Override
        public boolean test(Bundle trackedBundle) {
            return BundleUtil.hasHeader(trackedBundle, QUERY_MODELS);
        }
    }

    private class QueryRegisterCallback implements BundleCallback {

        BundleContext bundleContext;

        public QueryRegisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }


        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, QUERY_MODELS);


            for (Map<String, String> params : entries) {
                String key = params.get(QueryModel.NAME);
                if (queryModelRegistrations.containsKey(key)) {
                    log.error("Query model already loaded: " + key);
                } else {
                    // Unpack model
                    try {
                                QueryModel queryModel = QueryModel.loadQueryModel(
                                QueryModel.LoadArguments.queryLoadArgumentsBuilder()
                                        .inputStream(trackedBundle.getEntry(params.get("file")).openStream())
                                        .name(params.get(QueryModel.NAME))
                                        .version(trackedBundle.getVersion().toString()));

                        log.info("Registering Query model: " + queryModel);

                        ServiceRegistration<QueryModel> modelServiceRegistration = bundleContext.registerService(QueryModel.class, queryModel, queryModel.toDictionary());
                        queryModels.put(key, queryModel);
                        queryModelRegistrations.put(key, modelServiceRegistration);

                    } catch (IOException | QueryModel.QueryValidationException e) {
                        log.error("Could not load Query model: " + params.get(QueryModel.NAME) + " from bundle: " + trackedBundle.getBundleId(), e);
                    }
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

    private class QueryUnregisterCallback implements BundleCallback {

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, QUERY_MODELS);
            for (Map<String, String> params : entries) {
                String key = params.get(QueryModel.NAME);

                if (queryModels.containsKey(key)) {
                    ServiceRegistration<QueryModel> modelServiceRegistration = queryModelRegistrations.get(key);

                    if (modelServiceRegistration != null) {
                        log.info("Unregistering Query model: " + queryModels.get(key));
                        modelServiceRegistration.unregister();
                        queryModelRegistrations.remove(key);
                        queryModels.remove(key);
                    }
                } else {
                    log.error("Query Model is not registered: " + key);
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

}
