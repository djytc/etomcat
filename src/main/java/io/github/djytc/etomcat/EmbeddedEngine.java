package io.github.djytc.etomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardEngine;

/**
 * @author alexkasko
 *         Date: 9/22/14
 */
public class EmbeddedEngine extends StandardEngine {
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        this.startStopExecutor = new SameThreadExecutor();
    }
}
