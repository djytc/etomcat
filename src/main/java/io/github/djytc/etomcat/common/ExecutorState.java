package io.github.djytc.etomcat.common;

import org.apache.catalina.core.StandardThreadExecutor;

/**
 * User: wmel
 * Date: 27.06.12
 */
public class ExecutorState {
    private final int maxIdleTime;
    private final int maxThreads;
    private final int minSpareThreads;

    private final int activeCount;
    private final long completedCount;

    private final int corePoolSize;
    private final int largestPoolSize;

    private final int poolSize;
    private final int queueSize;

    public ExecutorState(StandardThreadExecutor ex) {
        this.maxIdleTime = ex.getMaxIdleTime();
        this.maxThreads = ex.getMaxThreads();
        this.minSpareThreads = ex.getMinSpareThreads();
        this.activeCount = ex.getActiveCount();
        this.completedCount = ex.getCompletedTaskCount();
        this.corePoolSize = ex.getCorePoolSize();
        this.largestPoolSize = ex.getLargestPoolSize();
        this.poolSize = ex.getPoolSize();
        this.queueSize = ex.getQueueSize();
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getLargestPoolSize() {
        return largestPoolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ExecutorState");
        sb.append("{maxIdleTime=").append(maxIdleTime);
        sb.append(", maxThreads=").append(maxThreads);
        sb.append(", minSpareThreads=").append(minSpareThreads);
        sb.append(", activeCount=").append(activeCount);
        sb.append(", completedCount=").append(completedCount);
        sb.append(", corePoolSize=").append(corePoolSize);
        sb.append(", largestPoolSize=").append(largestPoolSize);
        sb.append(", poolSize=").append(poolSize);
        sb.append(", queueSize=").append(queueSize);
        sb.append('}');
        return sb.toString();
    }
}
