# `osgi/src/main/java/hu/blackbelt/judo/meta/query/osgi` — bundle-tracking glue that publishes Query models as OSGi services

| File | Purpose |
|---|---|
| `QueryModelBundleTracker.java` | Immediate `@Component` registering a `QueryModel` service per bundle carrying the `Query-Models` header (`QUERY_MODELS`). `activate` installs register/unregister callbacks into the referenced `BundleTrackerManager`, `deactivate` removes them. Header entry needs `name` and `file`; `name` is the registration key, so a duplicate is logged and skipped, and a load failure is logged, never rethrown. |
