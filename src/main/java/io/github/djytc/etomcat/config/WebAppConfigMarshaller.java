package io.github.djytc.etomcat.config;

import io.github.djytc.etomcat.EmbeddedTomcatException;
import io.github.djytc.etomcat.jaxb.webapp.ObjectFactory;
import io.github.djytc.etomcat.jaxb.webapp.WebAppType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

/**
 * User: alexkasko
 * Date: 5/10/17
 */
public class WebAppConfigMarshaller {
    private static JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(WebAppType.class.getPackage().getName());
        } catch (Exception e) {
            throw new EmbeddedTomcatException("JAXB WebApp init error", e);
        }
    }

    public static String webAppConfigToXml(WebAppType webapp) {
        try {
            Marshaller marshaller = JAXB.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            ObjectFactory of = new ObjectFactory();
            StringWriter writer = new StringWriter();
            marshaller.marshal(of.createWebApp(webapp), writer);
            return writer.toString();
        } catch (Exception e) {
            throw new EmbeddedTomcatException("WebApp config marshalling error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static WebAppType webAppConfigFromXml(String xml) {
        try {
            Unmarshaller unmarshaller = JAXB.createUnmarshaller();
            JAXBElement<WebAppType> el = (JAXBElement<WebAppType>) unmarshaller.unmarshal(new StringReader(xml));
            return el.getValue();
        } catch (Exception e) {
            throw new EmbeddedTomcatException("WebApp config unmarshalling error", e);
        }
    }
}
