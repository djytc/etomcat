package io.github.djytc.etomcat;

import io.github.djytc.etomcat.jaxb.config.*;
import io.github.djytc.etomcat.jaxb.webapp.WebAppType;
import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.*;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.scan.StandardJarScanner;
import io.github.djytc.etomcat.failfast.FailFastConnector;
import io.github.djytc.etomcat.valves.PostCharacterEncodingValve;

import java.io.IOException;
import java.nio.file.Files;

/**
 * User: alexkasko
 * Date: 12/3/14
 */
public class EmbeddedTomcat {
    private final Log logger = LogFactory.getLog(EmbeddedTomcat.class);

    private final ETomcatConfigType cf;
    private final WebAppType webapp;
    
    private boolean started = false;
    private Server embedded;

    public EmbeddedTomcat(ETomcatConfigType cf, WebAppType webapp) {
        this.cf = cf;
        this.webapp = webapp;
    }

    public synchronized EmbeddedTomcat start() {
        if (started) throw new RuntimeException("ETomcat is already started");
        try {
            doStart();
            return this;
        } catch (LifecycleException e) {
            doStop();
            throw new RuntimeException("Exception occurred on starting ETomcat", e);
        }
    }

    public synchronized void stop() {
        if (started) doStop();
    }

    private void doStart() throws LifecycleException {
        logger.info("Starting Embedded Tomcat...");
        // creating
        embedded = createServer();
        StandardService service = createService();
        embedded.addService(service);
        Executor executor = createExecutor();
        service.addExecutor(executor);
        StandardEngine engine = createEngine(service);
        Host host = createHost();
        StandardContext context = createContext();
        Connector connector = createConnector();
        // tomcat binding
        service.setContainer(engine);
        engine.setDefaultHost(host.getName());
        engine.addChild(host);
        host.addChild(context);
        Http11NioProtocol proto = (Http11NioProtocol) connector.getProtocolHandler();
        proto.setExecutor(executor);
        service.addConnector(connector);
        // bind context for inheritors
        bindContext(context);
        // starting
        embedded.start();
        started = true;
        logger.info("Embedded Tomcat started successfully");
    }

    protected void bindContext(StandardContext context) {
    }

    private void doStop() {
        logger.info("Stopping Embedded Tomcat...");
        try {
            embedded.stop();
        } catch (Exception e) {
            logger.warn("Exception occured on stopping etomcat", e);
        }
        logger.info("Embedded Tomcat stopped");
    }

    private StandardService createService() {
        EmbeddedService service = new EmbeddedService();
        service.setName(getClass().getName());
        return service;
    }

    private StandardServer createServer() {
        StandardServer server = new StandardServer();
        // todo
//        server.setCatalinaBase(bd);
//        server.setCatalinaHome(bd);
        server.setPort(-1);
        return server;
    }

    private StandardEngine createEngine(Service service) {
        EmbeddedEngine engine = new EmbeddedEngine();
        engine.setService(service);
        engine.setName(getClass().getName());
        return engine;
    }

    private StandardHost createHost() {
        StandardHost host = new StandardHost();
        host.setAutoDeploy(false);
        host.setDeployOnStartup(false);
        host.setDeployXML(false);
        host.setUnpackWARs(false);

        host.setAppBase("NOT_SUPPORTED_SETTING");
        host.setName(cf.getGeneralConfig().getHostname());
        host.setErrorReportValveClass("org.apache.catalina.valves.ErrorReportValve");
        //todo: delete
        try {
            host.setWorkDir(Files.createTempDirectory("etomcat").toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return host;
    }

    private StandardContext createContext() {
        ContextConfigType contextConf = cf.getContextConfig();
        StandardContext context = new StandardContext();
//        context.setAltDDName(paths.getWebXmlFile());
        context.setCrossContext(true);
        context.setPrivileged(true);
        context.setClearReferencesHttpClientKeepAliveThread(false);
        StandardJarScanner scanner = (StandardJarScanner) context.getJarScanner();
        scanner.setScanClassPath(false);
        scanner.setScanAllDirectories(false);
        scanner.setScanAllFiles(false);
        scanner.setScanBootstrapClassPath(false);
        scanner.setScanManifest(false);
        context.setUnpackWAR(false);
        context.setUseNaming(false);
        context.setConfigured(true);
        context.setIgnoreAnnotations(true);
        context.setWrapperClass(EmbeddedWrapper.class.getName());

        context.setPath(cf.getGeneralConfig().getContextPath());        
        context.setCookies(contextConf.isCookies());
        // todo
//        context.setDocBase(paths.getDocBaseDir());
        context.setUnloadDelay(contextConf.getUnloadDelayMs());
        context.setSessionTimeout(contextConf.getSessionTimeoutMinutes());

        if(contextConf.getPostCharacterEncoding().length() > 0) {
            logger.debug("UTF-8 encoding enabled for all requests");
            Valve valve = new PostCharacterEncodingValve(contextConf.getPostCharacterEncoding());
            context.addValve(valve);
        }

        context.setLoader(new EmbeddedLoader());
//        todo
//        if(cf.getGeneralConfig().isUseFsResources()) {
//            logger.debug("Using FileDirContext");
//            context.setResources(new StandardRoot(context));
//        } else {
            logger.debug("Using EmbeddedDirContext");
            context.setResources(new EmbeddedResourceRoot(context));
//        }
        context.getResources().setCacheMaxSize(contextConf.getCacheMaxSizeKb());
        context.getResources().setCacheObjectMaxSize(contextConf.getCacheObjectMaxSizeKb());
        context.getResources().setCacheTtl(contextConf.getCacheTtlSec());
        context.getResources().setCachingAllowed(contextConf.isCachingAllowed());

        context.setManager(createManager());
        context.addLifecycleListener(new EmbeddedContextConfig(context, webapp));
        return context;
    }

    private EmbeddedManager createManager() {
        EmbeddedManager manager = new EmbeddedManager();
        manager.setMaxActiveSessions(cf.getContextConfig().getMaxActiveSessions());
        return manager;
    }

    private Connector createConnector() {
        ConnectorConfigType connectorConf = cf.getConnectorConfig();
        NioConfigType nioConf = cf.getNioConfig();
        SocketConfigType socketConf = cf.getSocketConfig();
        SslConfigType sslConf = cf.getSslConfig();
        Connector con = null;
        try {
            if(connectorConf.isUseFailFastResponseWriter()) {
                con = new FailFastConnector("org.apache.coyote.http11.Http11NioProtocol");
            } else {
                con = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Http11NioProtocol proto = (Http11NioProtocol) con.getProtocolHandler();
        con.setEnableLookups(connectorConf.isEnableLookups());
        con.setMaxPostSize(connectorConf.getMaxPostSizeBytes());
        con.setURIEncoding(connectorConf.getUriEncoding());
        con.setProperty("acceptCount", Integer.toString(connectorConf.getAcceptCount()));
        proto.setCompressibleMimeType(connectorConf.getCompressableMimeType());
        proto.setCompression(connectorConf.getCompression());
        proto.setCompressionMinSize(connectorConf.getCompressionMinSizeBytes());
        if(!connectorConf.getNoCompressionUserAgents().isEmpty()) proto.setNoCompressionUserAgents(connectorConf.getNoCompressionUserAgents());
        proto.setDisableUploadTimeout(connectorConf.isDisableUploadTimeout());
        proto.setMaxHttpHeaderSize(connectorConf.getMaxHttpHeaderSizeBytes());
        proto.setMaxKeepAliveRequests(connectorConf.getMaxKeepAliveRequests());
        con.setPort(cf.getGeneralConfig().getTcpPort());
        proto.setServer(connectorConf.getServer());

        proto.setUseSendfile(nioConf.isUseSendfile());
        con.setProperty("acceptorThreadCount", Integer.toString(nioConf.getAcceptorThreadCount()));
        proto.setAcceptorThreadPriority(nioConf.getAcceptorThreadPriority());
        proto.setPollerThreadCount(nioConf.getPollerThreadCount());
        proto.setPollerThreadPriority(nioConf.getPollerThreadPriority());
        proto.setSelectorTimeout(nioConf.getSelectorTimeoutMs());
//        proto.getEndpoint().setBindOnInit(false);
        con.setProperty("useComet", Boolean.toString(connectorConf.isUseComet()));
        // made all socket options lazy to prevent crash on winxp
        if(socketConf.isDirectBuffer()) con.setProperty("socket.directBuffer", Boolean.toString(true));
        if(25188 != socketConf.getRxBufSizeBytes()) con.setProperty("socket.rxBufSize", Integer.toString(socketConf.getRxBufSizeBytes()));
        if(43800 != socketConf.getTxBufSizeBytes()) con.setProperty("socket.txBufSize", Integer.toString(socketConf.getTxBufSizeBytes()));
        if(8192 != socketConf.getAppReadBufSizeBytes()) con.setProperty("socket.appReadBufSize", Integer.toString(socketConf.getAppReadBufSizeBytes()));
        if(8192 != socketConf.getAppWriteBufSizeBytes()) con.setProperty("socket.appWriteBufSize", Integer.toString(socketConf.getAppWriteBufSizeBytes()));
        if(500 != socketConf.getBufferPool()) con.setProperty("socket.bufferPool", Integer.toString(socketConf.getBufferPool()));
        if(104857600 != socketConf.getBufferPoolSizeBytes()) con.setProperty("socket.bufferPoolSize", Integer.toString(socketConf.getBufferPoolSizeBytes()));
        if(500 != socketConf.getProcessorCache())con.setProperty("socket.processorCache", Integer.toString(socketConf.getProcessorCache()));
        if(500 != socketConf.getKeyCache()) con.setProperty("socket.keyCache", Integer.toString(socketConf.getKeyCache()));
        if(500 != socketConf.getEventCache()) con.setProperty("socket.eventCache", Integer.toString(socketConf.getEventCache()));
        if(socketConf.isTcpNoDelay()) con.setProperty("socket.tcpNoDelay", Boolean.toString(true));
        if(socketConf.isSoKeepAlive()) con.setProperty("socket.soKeepAlive", Boolean.toString(true));
        if(!socketConf.isOoBInline()) con.setProperty("socket.ooBInline", Boolean.toString(false));
        if(!socketConf.isSoReuseAddress()) con.setProperty("socket.soReuseAddress", Boolean.toString(false));
        if(!socketConf.isSoLingerOn()) con.setProperty("socket.soLingerOn", Boolean.toString(false));
        if(25 != socketConf.getSoLingerTimeSec())con.setProperty("socket.soLingerTime", Integer.toString(socketConf.getSoLingerTimeSec()));
        if(5000 != socketConf.getSoTimeoutMs()) con.setProperty("socket.soTimeout", Integer.toString(socketConf.getSoTimeoutMs()));
        if((0x04 | 0x08 | 0x010) != socketConf.getSoTrafficClass()) con.setProperty("socket.soTrafficClass", Integer.toString(socketConf.getSoTrafficClass()));
        if(1 != socketConf.getPerformanceConnectionTime()) con.setProperty("socket.performanceConnectionTime", Integer.toString(socketConf.getPerformanceConnectionTime()));
        if(0 != socketConf.getPerformanceLatency()) con.setProperty("socket.performanceLatency", Integer.toString(socketConf.getPerformanceLatency()));
        if(1 != socketConf.getPerformanceBandwidth()) con.setProperty("socket.performanceBandwidth", Integer.toString(socketConf.getPerformanceBandwidth()));
        if(250 != socketConf.getUnlockTimeoutMs())con.setProperty("socket.unlockTimeout", Integer.toString(socketConf.getUnlockTimeoutMs()));
        con.setProperty("selectorPool.maxSelectors", Integer.toString(nioConf.getMaxSelectors()));
        con.setProperty("selectorPool.maxSpareSelectors", Integer.toString(nioConf.getMaxSpareSelectors()));

        if(sslConf.isSslEnabled()) {
            proto.setSSLEnabled(true);
            con.setScheme("https");
            con.setSecure(true);
            proto.setKeystoreFile(sslConf.getKeystoreFile());
            proto.setKeystorePass(sslConf.getKeystorePass());
            proto.setKeystoreType(sslConf.getKeystoreType());
            proto.setProperty("keystoreProvider", sslConf.getKeystoreProvider());
            proto.setKeyAlias(sslConf.getKeyAlias());
            proto.setAlgorithm(sslConf.getAlgorithm());
            if(sslConf.isClientAuth()) {
                proto.setClientAuth("true");
                proto.setTruststoreFile(sslConf.getTruststoreFile());
                proto.setTruststorePass(sslConf.getTruststorePass());
                proto.setTruststoreType(sslConf.getTruststoreType());
                con.setProperty("truststoreProvider", sslConf.getTruststoreProvider());
            }
            proto.setSslProtocol(sslConf.getProtocol());
            proto.setCiphers(sslConf.getCiphers());
            con.setProperty("sessionCacheSize", Integer.toString(sslConf.getSessionCacheSize()));
            con.setProperty("sessionTimeout", Integer.toString(sslConf.getSessionTimeoutSecs()));
            if(!sslConf.getCrlFile().isEmpty()) con.setProperty("crlFile", sslConf.getCrlFile());
        }

        return con;
    }

    private Executor createExecutor() {
        ExecutorConfigType executorConf = cf.getExecutorConfig();
        StandardThreadExecutor executor = new StandardThreadExecutor();
        executor.setThreadPriority(executorConf.getThreadPriority());
		executor.setName(executorConf.getName());
		executor.setDaemon(executorConf.isDaemon());
		executor.setNamePrefix(executorConf.getNamePrefix());
		executor.setMaxThreads(executorConf.getMaxThreads());
		executor.setMinSpareThreads(executorConf.getMinSpareThreads());
		executor.setMaxIdleTime(executorConf.getMaxIdleTimeMs());
		return executor;
    }

    public ExecutorState getInnerExecutorState() {
        StandardThreadExecutor executorImpl = (StandardThreadExecutor) embedded.findServices()[0].findExecutors()[0];
        return new ExecutorState(executorImpl.getMaxIdleTime(),
                executorImpl.getMaxThreads(),
                executorImpl.getMinSpareThreads(),
                executorImpl.getActiveCount(),
                executorImpl.getCompletedTaskCount(),
                executorImpl.getCorePoolSize(),
                executorImpl.getLargestPoolSize(),
                executorImpl.getPoolSize(),
                executorImpl.getQueueSize());
    }

    public Runnable shutdownHook() {
        return new ShutdownHook();
    }

    private class ShutdownHook implements Runnable {
        @Override
        public void run() {
            stop();
        }
    }
}
