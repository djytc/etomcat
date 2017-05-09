package io.github.djytc.etomcat.common;

import io.github.djytc.etomcat.ETomcatException;
import io.github.djytc.etomcat.jaxb.webapp.ObjectFactory;
import io.github.djytc.etomcat.jaxb.webapp.WebAppType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

import static io.github.djytc.etomcat.ETomcat.logger;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

/**
 * User: alexkasko
 * Date: 5/9/17
 */
public class WebAppMarshaller {
    private static JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(WebAppType.class.getPackage().getName());
        } catch (Exception e) {
            throw new ETomcatException("JAXB init error", e);
        }
    }

    public static String marshal(WebAppType webapp) {
        try {
            Marshaller marshaller = JAXB.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            ObjectFactory of = new ObjectFactory();
            StringWriter writer = new StringWriter();
            marshaller.marshal(of.createWebApp(webapp), writer);
            String res = writer.toString();
            logger.debug(res);
            return res;
        } catch (Exception e) {
            throw new ETomcatException("'web-app.xml' marshalling error", e);
        }
    }
}
