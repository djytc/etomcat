package io.github.djytc.etomcat.components;

import org.apache.catalina.*;
import org.apache.catalina.session.ManagerBase;
import org.apache.tomcat.util.ExceptionUtils;

import java.io.*;

/**
 * User: alexey
 * Date: 8/27/11
 */
public class EmbeddedManager extends ManagerBase {

    /**
     * @inheritDoc
     */
    @Override
    public String getName() {
        return getClass().getName();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void load() throws ClassNotFoundException, IOException {
        // noop
    }

    /**
     * @inheritDoc
     */
    @Override
    public void unload() throws IOException {
        // noop
    }

    /**
     * @inheritDoc
     */
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        setState(LifecycleState.STARTING);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setState(LifecycleState.STOPPING);

        // Expire all active sessions
        Session sessions[] = findSessions();
        for (int i = 0; i < sessions.length; i++) {
            Session session = sessions[i];
            try {
                if (session.isValid()) {
                    session.expire();
                }
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
            } finally {
                // Measure against memory leaking if references to the session
                // object are kept in a shared field somewhere
                session.recycle();
            }
        }

        // Require a new random number generator if we are restarted
        super.stopInternal();
    }

}
