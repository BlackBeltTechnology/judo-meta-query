# `osgi-itest/src/test/resources/etc` — Karaf `etc/` overrides swapped in by `KarafFeatureProvider`

| File | Purpose |
|---|---|
| `org.ops4j.pax.logging.cfg` | pax-logging (log4j2) config always swapped over the container's own by `replaceConfigurationFile`. Root level `DEBUG`; appenders `Console`, `RollingFile` (`${karaf.log}/karaf.log`, 16MB rollover), `PaxOsgi`. Console is throttled by a ThresholdFilter at `${karaf.log.console:-INFO}`, so console verbosity needs `-Dkaraf.log.console`, not the root level. Trailing stray `s` line. |
| `org.ops4j.pax.url.mvn.cfg` | pax-url-aether config installed only under `-DuseCustomSettings=true`, otherwise unused. Points `org.ops4j.pax.url.mvn.settings` at `${basedir}/../.maven.xml`, sets `useFallbackRepositories=false` and `certificateCheck=true`, pins central plus Apache/OPS4J snapshots, timeouts 5000 ms connect and 30000 ms read, `retryCount` 3. Missing `.maven.xml` fails every `mvn:` URL. |
