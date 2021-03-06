package io.github.djytc.etomcat.failfast;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Response;

/**
 * User: alexkasko
 * Date: 4/8/14
 */
public class FailFastConnector extends Connector {
    public FailFastConnector() throws Exception {
    }

    public FailFastConnector(String protocol) {
        super(protocol);
    }

    @Override
    public Response createResponse() {
        Response response = new FailFastResponse();
        response.setConnector(this);
        return (response);
    }
}
