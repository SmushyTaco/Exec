package com.smushytaco.exec;

import org.apache.commons.exec.ExecuteWatchdog;

import java.time.Duration;
import java.util.concurrent.Executors;

public class StopCheckExecuteWatchdog extends ExecuteWatchdog {
    private volatile boolean stopped = false;

    /**
     * Creates a new watchdog with a given timeout.
     *
     * @param timeoutMillis the timeout for the process in milliseconds. It must be greater than 0
     *     or 'INFINITE_TIMEOUT'
     */
    public StopCheckExecuteWatchdog(long timeoutMillis) {
        super(Duration.ofMillis(timeoutMillis), Executors.defaultThreadFactory());
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void stop() {
        super.stop();
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }
}
