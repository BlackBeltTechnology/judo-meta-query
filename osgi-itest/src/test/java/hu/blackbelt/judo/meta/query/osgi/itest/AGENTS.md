# `osgi-itest/src/test/java/hu/blackbelt/judo/meta/query/osgi/itest` — Pax Exam / Karaf integration tests for the OSGi bundle

| File | Purpose |
|---|---|
| `KarafFeatureProvider.java` | Boots the Pax Exam / Karaf container shared by the ITests. Exports `karafConfig(Class)`, `configureVmOptions()`, `getOsgiService(BundleContext, Class, String, long)`, `assertBundleStarted`, `testTargetDir`. Features load from `target/test-classes/test-features.xml`, so tests run only after resource copying; `getConfigFile` throws when the `/etc/...` resource is absent. |
| `QueryModelLoadITest.java` | `@Ignore`d Pax Exam test (`PerClass` reactor) proving `QueryModelBundleTracker` publishes a `QueryModel` service from a bundle header. Provisions the `hu.blackbelt.judo.meta.query.osgi` bundle, zeta `annotations`/`common`/`validation-core`, and a TinyBundle declaring `Query-Models` over `northwind-query.model`. `testEvlModelValidation` and `testJavaModelValidation` exercise both engines, but stay disabled. |
