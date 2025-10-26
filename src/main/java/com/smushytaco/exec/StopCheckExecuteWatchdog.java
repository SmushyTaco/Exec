package com.smushytaco.exec;

import org.apache.commons.exec.ExecuteWatchdog;

import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * An extension of {@link ExecuteWatchdog} that tracks
 * whether the watchdog has been explicitly stopped.
 *
 * <p>This class adds the ability to query the stopped state via {@link #isStopped()},
 * since the standard {@code ExecuteWatchdog} does not expose this information.
 *
 * <p>Used internally by {@link ManagedProcess} to determine whether a process
 * has been externally terminated or has naturally completed.
 *
 * @author Michael Vorburger
 */
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

    @Override
    public synchronized void stop() {
        super.stop();
        stopped = true;
    }

    /**
     * Checks whether this watchdog has been stopped.
     *
     * <p>This method returns {@code true} if {@link #stop()} has been called,
     * regardless of whether the monitored process is still running.
     *
     * @return {@code true} if the watchdog has been stopped; {@code false} otherwise
     */
    public boolean isStopped() {
        return stopped;
    }
}
