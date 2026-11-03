# `osgi-itest/src/test/resources` — Karaf provisioning input copied to `target/test-classes` for Pax Exam

| File | Purpose |
|---|---|
| `test-features.xml` | Karaf features repository `judo-TEST` (schema v1.5.0), loaded by `KarafFeatureProvider.karafConfig` from `target/test-classes` and installed under the feature id `test`: prerequisites `wrap`, `shell`, `scr`, then `osgi-utils`, `tinybundles`, `epsilon-runtime`, `cxf-jaxrs`. Repository URLs interpolate `${epsilon-runtime-version}` and `${karaf-features-version}`, so it resolves only after Maven resource filtering. |
