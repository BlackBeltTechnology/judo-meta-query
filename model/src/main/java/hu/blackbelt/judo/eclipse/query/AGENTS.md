# `model/src/main/java/hu/blackbelt/judo/eclipse/query` — Eclipse plugin lifecycle for the Query metamodel bundle

| File | Purpose |
|---|---|
| `Activator.java` | Eclipse UI plug-in lifecycle hook, extends `AbstractUIPlugin`. Declares `PLUGIN_ID = "hu.blackbelt.judo.meta.query"` and a static singleton exposed by `getDefault()`; `start(BundleContext)` assigns it, `stop(BundleContext)` nulls it. `getDefault()` therefore returns `null` outside an active Eclipse/OSGi session, so nothing on the plain-Maven or OSGi-bundle path may depend on it. `PLUGIN_ID` must match the bundle symbolic name in `model/META-INF/MANIFEST.MF`. |
