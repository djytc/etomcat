import io.github.djytc.etomcat.ETomcat;
import io.github.djytc.etomcat.jaxb.config.*;
import io.github.djytc.etomcat.jaxb.webapp.*;
import io.github.djytc.etomcat.jaxb.webapp.ObjectFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.jsslutils.extra.apachehttpclient.SslContextedSecureProtocolSocketFactory;
import org.junit.Test;
import test.TestServlet;

import javax.net.ssl.*;
import java.io.File;
import java.lang.String;
import java.net.URI;
import java.security.*;

import static org.junit.Assert.assertEquals;

/**
 * User: alexkasko
 * Date: 4/25/17
 */
public class ETomcatTest {

    @Test
    public void test() throws Exception {
        ETomcat tc = new ETomcat(createTomcatConfig(), createWebApp());
        tc.start();
        HttpClient client = new HttpClient();
        // test get
        setupClientSsl();
        HttpMethod get = new GetMethod("https://127.0.0.1:8443/etomcat_test");
        get.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        client.executeMethod(get);
        byte[] responseBody = get.getResponseBody();
        java.lang.String content = new java.lang.String(responseBody, "UTF-8");
        assertEquals(TestServlet.class.getSimpleName(), content);
        System.out.println(content);
        System.out.println(tc.getInternalExecutorState());
    }

    private static WebAppType createWebApp() {
        ObjectFactory of = new ObjectFactory();
        return new WebAppType()
                .withVersion("3.1")
                .withId("test")
                .withModuleNameOrDescriptionAndDisplayName(
                        of.createWebAppTypeServlet(new ServletType()
                                .withServletName(
                                        new ServletNameType()
                                                .withValue("testServlet"))
                                .withServletClass(
                                        new FullyQualifiedClassType()
                                                .withValue("test.TestServlet"))),
                        of.createWebAppTypeServletMapping(new ServletMappingType()
                                .withServletName(
                                        new ServletNameType()
                                                .withValue("testServlet"))
                                .withUrlPattern(
                                        new UrlPatternType()
                                                .withValue("/etomcat_test"))),
                        of.createWebAppTypeListener(new ListenerType()
                                .withListenerClass(
                                        new FullyQualifiedClassType()
                                                .withValue("test.TestListener"))),
                        of.createWebAppTypeFilter(new FilterType()
                                .withFilterName(
                                        new FilterNameType()
                                                .withValue("testFilter"))
                                .withFilterClass(
                                        new FullyQualifiedClassType()
                                                .withValue("test.TestFilter"))),
                        of.createWebAppTypeFilterMapping(new FilterMappingType()
                                .withFilterName(
                                        new FilterNameType()
                                                .withValue("testFilter"))
                                .withUrlPatternOrServletName(
                                        new UrlPatternType()
                                                .withValue("/etomcat_test")))
                );
    }

    private static TomcatConfigType createTomcatConfig() {
        return new TomcatConfigType(new ConnectorConfigType(), new ContextConfigType(), new ExecutorConfigType(),
                new GeneralConfigType(), new NioConfigType(), new SocketConfigType(), new SslConfigType())
                .withGeneralConfig(new GeneralConfigType()
                        .withTcpPort(8443))
                .withSslConfig(new SslConfigType()
                        .withSslEnabled(true)
                        .withKeystoreFile(new File(codeSourceDir(ETomcatTest.class), "../../src/test/resources/etomcat.p12").getAbsolutePath())
                        .withKeystorePass("storepass")
                        .withKeyAlias("etomcat_test"));
    }

    private static void setupClientSsl() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS", "SUN");
        trustStore.load(ETomcatTest.class.getResourceAsStream("/client-truststore.jks"), "amber%".toCharArray());
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
        fac.init(trustStore);
        TrustManager[] tms = fac.getTrustManagers();
        SSLContext ctx = SSLContext.getInstance("TLS", "SunJSSE");
        ctx.init(new KeyManager[]{}, tms, new SecureRandom());
        SslContextedSecureProtocolSocketFactory secureProtocolSocketFactory = new SslContextedSecureProtocolSocketFactory(ctx);
        Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) secureProtocolSocketFactory, 8443));
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
