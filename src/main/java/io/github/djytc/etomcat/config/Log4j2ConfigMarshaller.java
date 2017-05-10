package io.github.djytc.etomcat.config;


import io.github.djytc.etomcat.EmbeddedTomcatException;
import io.github.djytc.etomcat.jaxb.log4j2.ConfigurationType;
import io.github.djytc.etomcat.jaxb.log4j2.ObjectFactory;

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
public class Log4j2ConfigMarshaller {
    private static JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(ConfigurationType.class.getPackage().getName());
        } catch (Exception e) {
            throw new EmbeddedTomcatException("JAXB Log4j2 init error", e);
        }
    }

    public static String log4j2ConfigToXml(ConfigurationType conf) {
        try {
            Marshaller marshaller = JAXB.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            ObjectFactory of = new ObjectFactory();
            marshaller.marshal(of.createConfiguration(conf), writer);
            return writer.toString();
        } catch (Exception e) {
            throw new EmbeddedTomcatException("Log4j2 config marshalling error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static ConfigurationType log4j2ConfigFromXml(String xml) {
        try {
            Unmarshaller unmarshaller = JAXB.createUnmarshaller();
            JAXBElement<ConfigurationType> el = (JAXBElement<ConfigurationType>) unmarshaller.unmarshal(new StringReader(xml));
            return el.getValue();
        } catch (Exception e) {
            throw new EmbeddedTomcatException("Log4j2 config unmarshalling error", e);
        }
    }
}
