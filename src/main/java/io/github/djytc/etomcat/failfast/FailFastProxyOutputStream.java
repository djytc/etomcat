package io.github.djytc.etomcat.failfast;

import org.apache.catalina.connector.CoyoteOutputStream;
import org.apache.catalina.connector.OutputBuffer;

/**
 * User: alexkasko
 * Date: 4/8/14
 */
public class FailFastProxyOutputStream extends CoyoteOutputStream {
    protected FailFastProxyOutputStream(OutputBuffer ob) {
        super(ob);
    }
}
