package hu.blackbelt.judo.meta.query.osgi;

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
@Designate(ocd = QueryModelBundleTracker.TrackerConfig.class)
public class QueryModelBundleTracker {

    public static final String QUERY_MODELS = "Query-Models";

    @ObjectClassDefinition(name="Query Model Bundle Tracker")
    public @interface TrackerConfig {
        @AttributeDefinition(
                name = "Tags",
                description = "Which tags are on the loaded model when there is no one defined in bundle"
        )
        String tags() default "";
    }

    @Reference
    BundleTrackerManager bundleTrackerManager;

    Map<String, ServiceRegistration<QueryModel>> queryModelRegistrations = new ConcurrentHashMap<>();

    Map<String, QueryModel> queryModels = new HashMap<>();

    TrackerConfig config;

    @Activate
    public void activate(final ComponentContext componentContext, final TrackerConfig trackerConfig) {
        this.config = trackerConfig;
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
                    if (params.containsKey(QueryModel.META_VERSION_RANGE)) {
                        VersionRange versionRange = new VersionRange(params.get(QueryModel.META_VERSION_RANGE).replaceAll("\"", ""));
                        if (versionRange.includes(bundleContext.getBundle().getVersion())) {
                            // Unpack model
                            try {
                                        QueryModel queryModel = QueryModel.loadQueryModel(
                                        QueryModel.LoadArguments.queryLoadArgumentsBuilder()
                                                .inputStream(trackedBundle.getEntry(params.get("file")).openStream())
                                                .name(params.get(QueryModel.NAME))
                                                .version(trackedBundle.getVersion().toString())
                                                .checksum(Optional.ofNullable(params.get(QueryModel.CHECKSUM)).orElse("notset"))
                                                .tags(Stream.of(ofNullable(params.get(QueryModel.TAGS)).orElse(config.tags()).split(",")).collect(Collectors.toSet()))
                                                .acceptedMetaVersionRange(Optional.of(versionRange.toString()).orElse("[0,99)")));

                                log.info("Registering Query model: " + queryModel);

                                ServiceRegistration<QueryModel> modelServiceRegistration = bundleContext.registerService(QueryModel.class, queryModel, queryModel.toDictionary());
                                queryModels.put(key, queryModel);
                                queryModelRegistrations.put(key, modelServiceRegistration);

                            } catch (IOException | QueryModel.QueryValidationException e) {
                                log.error("Could not load Psm model: " + params.get(QueryModel.NAME) + " from bundle: " + trackedBundle.getBundleId(), e);
                            }
                        }
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
