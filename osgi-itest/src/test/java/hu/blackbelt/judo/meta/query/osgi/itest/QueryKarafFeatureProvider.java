package hu.blackbelt.judo.meta.query.osgi.itest;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TimeoutException;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

public class QueryKarafFeatureProvider {
    public static final String KARAF_GROUPID = "org.apache.karaf";
    public static final String APACHE_KARAF = "apache-karaf";
    public static final String KARAF_FEATURES_GROUPID = "org.apache.karaf.features";
    public static final String STANDARD = "standard";
    public static final String FEATURES = "features";
    public static final String XML = "xml";
    public static final String ZIP = "zip";
    public static final String SERVICEMIX_BUNDLES_GROUPID = "org.apache.servicemix.bundles";
    public static final String HAMCREST = "org.apache.servicemix.bundles.hamcrest";

    public static final Integer SERVICE_TIMEOUT = 30000;

    public static final String FEATURE_CXF_JAXRS = "cxf-jaxrs";
    public static final String FEATURE_CXF_JACKSON = "cxf-jackson";
    public static final String FEATURE_SWAGGER_CORE = "cxf-rs-description-swagger2";

    public static MavenArtifactUrlReference  karafUrl() {
        return maven()
                .groupId(KARAF_GROUPID)
                .artifactId(APACHE_KARAF)
                .versionAsInProject()
                .type(ZIP);
    }

    public static MavenArtifactUrlReference  karafStandardRepo() {
        return maven()
                .groupId(KARAF_FEATURES_GROUPID)
                .artifactId(STANDARD)
                .versionAsInProject()
                .classifier(FEATURES)
                .type(XML);
    }

    public static Option[] karafConfig(Class clazz) throws FileNotFoundException {

        return new Option[] {
                // KarafDistributionOption.debugConfiguration("5005", true),
                karafDistributionConfiguration()
                        .frameworkUrl(karafUrl())
                        .unpackDirectory(new File("target", "exam"))
                        .useDeployFolder(false),
                keepRuntimeFolder(),
                cleanCaches(true),
                logLevel(LogLevelOption.LogLevel.INFO),
                // Debug
                when(Boolean.getBoolean( "isDebugEnabled" ) ).useOptions(
                        vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005")
                ),
                //systemTimeout(30000),
                //debugConfiguration("5005", true),
                vmOption("-Dfile.encoding=UTF-8"),
                //systemProperty("pax.exam.service.timeout").value("30000"),
                replaceConfigurationFile("etc/org.ops4j.pax.logging.cfg",
                        getConfigFile(clazz, "/etc/org.ops4j.pax.logging.cfg")),

                when(Boolean.getBoolean( "useCustomSettings")).useOptions(
                        replaceConfigurationFile("etc/org.ops4j.pax.url.mvn.cfg",
                                getConfigFile(clazz,"/etc/org.ops4j.pax.url.mvn.cfg"))
                ),

                configureConsole().ignoreLocalConsole(),

                provision(
                        /*
                        mavenBundle()
                                .groupId("org.ops4j.pax.swissbox")
                                .artifactId("pax-swissbox-tracker")
                                .version("1.8.4_timeoutfix").start(),
                        */
                        mavenBundle()
                                .groupId(SERVICEMIX_BUNDLES_GROUPID)
                                .artifactId(HAMCREST)
                                .versionAsInProject().start()
                )
        };
    }

    public static File getConfigFile(Class clazz, String path) {
        URL res = clazz.getResource(path);
        if (res == null) {
            throw new RuntimeException("Config resource " + path + " not found");
        }
        return new File(res.getFile());
    }

    /**
     * Explodes the dictionary into a ,-delimited list of key=value pairs
     */
    public static String explode(Dictionary dictionary) {
        Enumeration keys = dictionary.keys();
        StringBuffer result = new StringBuffer();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            result.append(String.format("%s=%s", key, dictionary.get(key)));
            if (keys.hasMoreElements()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /*
     * Provides an iterable collection of references, even if the original array
     * is null
     */
    public static Collection<ServiceReference> asCollection(ServiceReference[] references) {
        return references != null ? Arrays.asList(references) : Collections.<ServiceReference> emptyList();
    }


    public static  <T> T getOsgiService(BundleContext bundleContext, Class<T> type, long timeout) {
        return getOsgiService(bundleContext, type, null, timeout);
    }

    public static  <T> T getOsgiService(BundleContext bundleContext, Class<T> type) {
        return getOsgiService(bundleContext, type, null, SERVICE_TIMEOUT);
    }

    public static  <T> T getOsgiService(BundleContext bundleContext, Class<T> type, String filter, long timeout) {
        ServiceTracker tracker = null;
        try {
            String flt;
            if (filter != null) {
                if (filter.startsWith("(")) {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")" + filter + ")";
                } else {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")(" + filter + "))";
                }
            } else {
                flt = "(" + Constants.OBJECTCLASS + "=" + type.getName() + ")";
            }
            Filter osgiFilter = FrameworkUtil.createFilter(flt);
            tracker = new ServiceTracker(bundleContext, osgiFilter, null);
            tracker.open(true);
            // Note that the tracker is not closed to keep the reference
            // This is buggy, as the service reference may change i think
            Object svc = type.cast(tracker.waitForService(timeout));
            if (svc == null) {
                Dictionary dic = bundleContext.getBundle().getHeaders();
                System.err.println("Test bundle headers: " + explode(dic));

                for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, null))) {
                    System.err.println("ServiceReference: " + ref);
                }

                for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, flt))) {
                    System.err.println("Filtered ServiceReference: " + ref);
                }

                throw new RuntimeException("Gave up waiting for service " + flt);
            }
            return type.cast(svc);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static File testTargetDir(Class clazz){
        String relPath = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath);
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }


    public static void assertBundleStarted(BundleContext bundleContext, String name) {
        Bundle bundle = findBundleByName(bundleContext, name);
        assertNotNull("Bundle " + name + " should be installed", bundle);
        assertEquals("Bundle " + name + " should be started", Bundle.ACTIVE, bundle.getState());
    }

    public static  Bundle findBundleByName(BundleContext bundleContext, String symbolicName) {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals(symbolicName)) {
                return bundle;
            }
        }
        return null;
    }

    public static  void waitWebPage(String urlSt) throws InterruptedException, TimeoutException {
        System.out.println("Waiting for url " + urlSt);
        HttpURLConnection con = null;
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                URL url = new URL(urlSt);
                con = (HttpURLConnection)url.openConnection();
                int status = con.getResponseCode();
                if (status == 200) {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String responseBody = br.lines().collect(Collectors.joining());
                    System.out.println("WADL returned:\n" + responseBody);
                    return;
                }
            } catch (ConnectException e) {
                // Ignore connection refused
            } catch (MalformedURLException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            sleepOrTimeout(startTime, 20, "Timeout waiting for web page " + urlSt);
        }
    }

    /**
     * Sleeps for a short interval, throwing an exception if timeout has been reached. Used to facilitate a
     * retry interval with timeout when used in a loop.
     *
     * @param startTime the start time of the entire operation in milliseconds
     * @param timeout the timeout duration for the entire operation in seconds
     * @param message the error message to use when timeout occurs
     * @throws InterruptedException if interrupted while sleeping
     */
    public static void sleepOrTimeout(long startTime, long timeout, String message)
            throws InterruptedException, TimeoutException {
        timeout *= 1000; // seconds to millis
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = timeout - elapsed;
        if (remaining <= 0) {
            throw new TimeoutException(message);
        }
        long interval = Math.min(remaining, 1000);
        Thread.sleep(interval);
    }

    public static MavenArtifactUrlReference apacheCxf() {
        return maven().groupId("org.apache.cxf.karaf").artifactId("apache-cxf").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltOsgiUtils() {
        return maven().groupId("hu.blackbelt.osgi.utils").artifactId("features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltGoogle() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("google-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltBouncCastle() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("bouncycastle-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltApacheCommons() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("apache-commons-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltApacheHttpClient() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("apache-httpclient-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltApachePoi() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("apache-poi-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltEclipseEmf() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("eclipse-emf-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltEclipseEpsilon() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("eclipse-epsilon-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltTinybundles() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("tinybundles-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltAntlr() {
        return maven().groupId("hu.blackbelt.karaf.features").artifactId("antlr-features").versionAsInProject().classifier("features").type("xml");
    }

    public static MavenArtifactUrlReference blackbeltEpsilonRuntime() {
        return maven().groupId("hu.blackbelt.epsilon").artifactId("features").versionAsInProject().classifier("features").type("xml");
    }

    public static Option[] getRuntimeFeaturesForMetamodel(Class clazz) throws FileNotFoundException {
        return combine(karafConfig(clazz),

                features(karafStandardRepo()),

                features(blackbeltBouncCastle()),

                features(blackbeltApacheCommons()),

                features(blackbeltApacheHttpClient()),

                features(blackbeltApachePoi()),

                features(blackbeltOsgiUtils(), "osgi-utils"),

                features(blackbeltGoogle()),

                features(blackbeltTinybundles(), "tinybundles"),

                features(blackbeltEclipseEmf()),

                features(blackbeltAntlr()),

                features(blackbeltEclipseEpsilon()),

                features(blackbeltEpsilonRuntime(), "epsilon-runtime"),

                features(apacheCxf(), FEATURE_SWAGGER_CORE, FEATURE_CXF_JACKSON, FEATURE_CXF_JAXRS),

                newConfiguration("hu.blackbelt.jaxrs.providers.JacksonProvider")
                        .put("JacksonProvider.SerializationFeature.INDENT_OUTPUT", "true").asOption(),

                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg",
                        "org.osgi.service.http.port", "8181"));
    }

}
