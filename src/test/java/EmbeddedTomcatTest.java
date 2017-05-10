import io.github.djytc.etomcat.EmbeddedTomcat;
import io.github.djytc.etomcat.config.Log4j2ConfigMarshaller;
import io.github.djytc.etomcat.jaxb.config.*;
import io.github.djytc.etomcat.jaxb.log4j2.*;
import io.github.djytc.etomcat.jaxb.webapp.*;
import io.github.djytc.etomcat.jaxb.webapp.FilterType;
import io.github.djytc.etomcat.jaxb.webapp.ObjectFactory;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.jsslutils.extra.apachehttpclient.SslContextedSecureProtocolSocketFactory;
import org.junit.Test;
import test.TestServlet;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.String;
import java.net.URI;
import java.security.*;

import static org.junit.Assert.assertEquals;

/**
 * User: alexkasko
 * Date: 4/25/17
 */
public class EmbeddedTomcatTest {

    static {
        initLog4j2();
    }

    @Test
    public void test() throws Exception {
        EmbeddedTomcat tc = new EmbeddedTomcat(createTomcatConfig(), createWebApp());
        tc.start();
        HttpClient client = new HttpClient();
        // test get
        setupClientSsl();
        HttpMethod get = new GetMethod("https://127.0.0.1:8443/etomcat_test");
//        get.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        client.executeMethod(get);
        String content = new String(get.getResponseBody(), "UTF-8");
        assertEquals(TestServlet.class.getSimpleName(), content);
        // static get
        HttpMethod getStatic = new GetMethod("https://127.0.0.1:8443/static/test.txt");
        client.executeMethod(getStatic);
        String contentStatic = new String(getStatic.getResponseBody(), "UTF-8");
        assertEquals("hello static!", contentStatic);
    }

    private static WebAppType createWebApp() {
        ObjectFactory of = new ObjectFactory();
        return new WebAppType()
                .withVersion("3.1")
                .withId("test")
                .withModuleNameOrDescriptionAndDisplayName(
                        // static content servlet
                        of.createWebAppTypeServlet(new ServletType()
                                .withServletName(new ServletNameType()
                                        .withValue("default"))
                                .withServletClass(new FullyQualifiedClassType()
                                        .withValue(DefaultServlet.class.getName()))
                                .withInitParam(new ParamValueType()
                                        .withParamName(new io.github.djytc.etomcat.jaxb.webapp.String()
                                                .withValue("debug"))
                                        .withParamValue(new XsdStringType()
                                                .withValue("0")))
                                .withInitParam(new ParamValueType()
                                        .withParamName(new io.github.djytc.etomcat.jaxb.webapp.String()
                                                .withValue("listings"))
                                        .withParamValue(new XsdStringType()
                                                .withValue("false")))
                                .withLoadOnStartup("1")
                        ),
                        of.createWebAppTypeServletMapping(new ServletMappingType()
                                .withServletName(new ServletNameType()
                                        .withValue("default"))
                                .withUrlPattern(new UrlPatternType()
                                        .withValue("/static/*"))),
                        // app servlet
                        of.createWebAppTypeServlet(new ServletType()
                                .withServletName(new ServletNameType()
                                        .withValue("testServlet"))
                                .withServletClass(new FullyQualifiedClassType()
                                        .withValue("test.TestServlet"))
                                .withLoadOnStartup("1")),
                        of.createWebAppTypeServletMapping(new ServletMappingType()
                                .withServletName(new ServletNameType()
                                        .withValue("testServlet"))
                                .withUrlPattern(new UrlPatternType()
                                        .withValue("/etomcat_test"))),
                        // app listener
                        of.createWebAppTypeListener(new ListenerType()
                                .withListenerClass(new FullyQualifiedClassType()
                                                .withValue("test.TestListener"))),
                        // app filter
                        of.createWebAppTypeFilter(new FilterType()
                                .withFilterName(new FilterNameType()
                                        .withValue("testFilter"))
                                .withFilterClass(new FullyQualifiedClassType()
                                        .withValue("test.TestFilter"))),
                        of.createWebAppTypeFilterMapping(new FilterMappingType()
                                .withFilterName(new FilterNameType()
                                        .withValue("testFilter"))
                                .withUrlPatternOrServletName(new UrlPatternType()
                                        .withValue("/etomcat_test")))
                );
    }

    private static TomcatConfigType createTomcatConfig() {
        File resourcesDir = new File(codeSourceDir(EmbeddedTomcatTest.class), "../../src/test/resources");
        return new TomcatConfigType(new ConnectorConfigType(), new ContextConfigType(), new ExecutorConfigType(),
                new GeneralConfigType(), new NioConfigType(), new SocketConfigType(), new SslConfigType())
                .withGeneralConfig(new GeneralConfigType()
                        .withTcpPort(8443)
                        .withDocBaseDir(new File(resourcesDir, "webroot").getAbsolutePath())
                        .withWebAppMount("/static"))
                .withSslConfig(new SslConfigType()
                        .withSslEnabled(true)
                        .withKeystoreFile(new File(resourcesDir, "etomcat.p12").getAbsolutePath())
                        .withKeystorePass("storepass")
                        .withKeyAlias("etomcat_test"));
    }

    private static void setupClientSsl() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS", "SUN");
        trustStore.load(EmbeddedTomcatTest.class.getResourceAsStream("/client-truststore.jks"), "amber%".toCharArray());
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
        fac.init(trustStore);
        TrustManager[] tms = fac.getTrustManagers();
        SSLContext ctx = SSLContext.getInstance("TLS", "SunJSSE");
        ctx.init(new KeyManager[]{}, tms, new SecureRandom());
        SslContextedSecureProtocolSocketFactory secureProtocolSocketFactory = new SslContextedSecureProtocolSocketFactory(ctx);
        Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) secureProtocolSocketFactory, 8443));
    }

    private static void initLog4j2() {
        try {
            ConfigurationType conf = createLog4j2Config();
            String xml = Log4j2ConfigMarshaller.log4j2ConfigToXml(conf);
            ByteArrayInputStream baos = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            ConfigurationSource src = new ConfigurationSource(baos);
            Configurator.initialize(Thread.currentThread().getContextClassLoader(), src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ConfigurationType createLog4j2Config() throws Exception {
        io.github.djytc.etomcat.jaxb.log4j2.ObjectFactory of = new io.github.djytc.etomcat.jaxb.log4j2.ObjectFactory();
        return new ConfigurationType()
                .withAppenders(new AppendersType()
                        .withConsole(new ConsoleType()
                                .withName("console")
                                .withTarget("SYSTEM_OUT")
                                .withPatternLayout(new PatternLayoutType()
                                        .withPattern("%d{yyyy-MM-dd HH:mm:ss} [%-5p %-10.10t %-30.30c] %m%n"))))
                .withLoggers(new LoggersType()
                        .withContent(
                                of.createLoggersTypeRoot(new RootType()
                                        .withLevel("debug")
                                        .withAppenderRef(new AppenderRefType()
                                                .withRef("console"))),
                                of.createLoggersTypeLogger(new LoggerType()
                                        .withName("org.apache.tomcat")
                                        .withLevel("warn")
                                        .withAppenderRef(new AppenderRefType()
                                                .withRef("console"))),
                                of.createLoggersTypeLogger(new LoggerType()
                                        .withName("org.apache.catalina")
                                        .withLevel("warn")
                                        .withAppenderRef(new AppenderRefType()
                                                .withRef("console"))),
                                of.createLoggersTypeLogger(new LoggerType()
                                        .withName("org.apache.coyote")
                                        .withLevel("warn")
                                        .withAppenderRef(new AppenderRefType()
                                                .withRef("console"))),
                                of.createLoggersTypeLogger(new LoggerType()
                                        .withName("httpclient")
                                        .withLevel("info")
                                        .withAppenderRef(new AppenderRefType()
                                                .withRef("console"))),
                                of.createLoggersTypeLogger(new LoggerType()
                                        .withName("org.apache.commons.httpclient")
                                        .withLevel("info")
                                        .withAppenderRef(new AppenderRefType()
                                                .withRef("console")))
                        ));
    }

    private static File codeSourceDir(Class<?> clazz) {
        try {
            URI uri = clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarOrDir = new File(uri);
            return jarOrDir.isDirectory() ? jarOrDir : jarOrDir.getParentFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
