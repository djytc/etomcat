package io.github.djytc.etomcat.config;

import io.github.djytc.etomcat.EmbeddedTomcatException;
import io.github.djytc.etomcat.jaxb.config.TomcatConfigType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

/**
 * User: alexkasko
 * Date: 5/10/17
 */
public class TomcatConfigMarshaller {
    private static JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(TomcatConfigType.class.getPackage().getName());
        } catch (Exception e) {
            throw new EmbeddedTomcatException("JAXB Tomcat init error", e);
        }
    }

    public static String tomcatConfigToXml(TomcatConfigType tc) {
        try {
            Marshaller marshaller = JAXB.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(tc, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new EmbeddedTomcatException("Tomcat config marshalling error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static TomcatConfigType tomcatConfigFromXml(String xml) {
        try {
            Unmarshaller unmarshaller = JAXB.createUnmarshaller();
            return  (TomcatConfigType) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            throw new EmbeddedTomcatException("Tomcat config unmarshalling error", e);
        }
    }
}
