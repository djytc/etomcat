package io.github.djytc.etomcat.components;

import io.github.djytc.etomcat.common.SameThreadExecutor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;

/**
 * User: alexkasko
 * Date: 5/9/17
 */
public class EmbeddedHost extends StandardHost {
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        this.startStopExecutor = new SameThreadExecutor();
    }
}
