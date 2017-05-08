import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.jsslutils.extra.apachehttpclient.SslContextedSecureProtocolSocketFactory;
import org.junit.Test;
import io.github.djytc.etomcat.EmbeddedTomcat;
import test.TestServlet;
import io.github.djytc.etomcat.config.GeneralProperties;
import io.github.djytc.etomcat.config.SslProperties;

import javax.net.ssl.*;
import java.io.File;
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
        EmbeddedTomcat tc = new EmbeddedTomcat();
        setupServerSsl(tc);
        Runtime.getRuntime().addShutdownHook(new Thread(tc.shutdownHook()));
        tc.start(new File("src/test/resources/appdir"));
        HttpClient client = new HttpClient();
        // test get
        setupClientSsl();
        HttpMethod get = new GetMethod("https://127.0.0.1:8443/etomcat_test");
        get.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        client.executeMethod(get);
        byte[] responseBody = get.getResponseBody();
        String content = new String(responseBody, "UTF-8");
        assertEquals(TestServlet.class.getSimpleName(), content);
        System.out.println(content);
        System.out.println(tc.getInnerExecutorState());
    }

    private static void setupServerSsl(EmbeddedTomcat tc) {
//        File keystore = new File(codeSourceDir(ETomcatTest.class), "../../src/test/resources/etomcat.p12");
        tc.setSslProps(new SslProperties()
                .setSslEnabled(true)
                .setKeystoreFile("../../etomcat.p12")
                .setKeystorePass("storepass")
                .setKeyAlias("etomcat_test"))
        .setGeneralProps(new GeneralProperties().setPort(8443));
    }

    private static void setupClientSsl() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS", "SUN");
        trustStore.load(ETomcatTest.class.getResourceAsStream("/client-truststore.jks"), "amber%".toCharArray());
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
        fac.init(trustStore);
        TrustManager[] tms = fac.getTrustManagers();
        SSLContext ctx = SSLContext.getInstance("TLS", "SunJSSE");
        ctx.init(new KeyManager[] {}, tms, new SecureRandom());
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
