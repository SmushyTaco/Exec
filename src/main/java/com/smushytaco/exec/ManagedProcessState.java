/*
 * Copyright 2025 Nikan Radan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smushytaco.exec;

import java.io.IOException;

/**
 * Read-only view and lifecycle controls for a managed external process.
 *
 * <p>Provides operations to start, wait, destroy, and query state, plus helpers for reading
 * recent console output useful for diagnostics.
 */
public interface ManagedProcessState {

    /**
     * Starts the process and waits until a specific message appears in the process console output
     * (stdout or stderr), or the timeout elapses.
     *
     * @param messageInConsole the text to wait for in the console output
     * @param maxWaitUntilReturning maximum time to wait in milliseconds before giving up
     * @return {@code true} if the message was seen before the timeout; {@code false} if timed out
     * @throws IOException if closing/decoding console streams fails while waiting
     * @throws ManagedProcessInterruptedException if the waiting thread is interrupted
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean startAndWaitForConsoleMessageMaxMs(String messageInConsole, long maxWaitUntilReturning)
            throws IOException, ManagedProcessInterruptedException;

    /**
     * Destroys (terminates) the running process.
     *
     * <p>If the process is not running, an exception is thrown.
     *
     * @throws ManagedProcessException if the process was already stopped or never started
     * @throws ManagedProcessInterruptedException if interrupted while waiting for termination
     */
    void destroy() throws ManagedProcessException, ManagedProcessInterruptedException;

    /**
     * Indicates whether the process has been started and has not yet terminated.
     *
     * @return {@code true} if the process is currently alive; {@code false} otherwise
     */
    boolean isAlive();

    /**
     * Internal callback used to signal that the process has halted (success or failure).
     *
     * <p>Intended for implementations to reconcile watchdog state vs. observed termination.
     */
    void notifyProcessHalted();

    /**
     * Returns the exit value of the process.
     *
     * @return the process exit code (conventionally {@code 0} indicates success)
     * @throws ManagedProcessException if the process has not yet terminated or no exit value exists
     * @throws ManagedProcessInterruptedException if interrupted while retrieving the exit value
     */
    @SuppressWarnings("UnusedReturnValue")
    int exitValue() throws ManagedProcessException, ManagedProcessInterruptedException;

    /**
     * Blocks until the process terminates and returns its exit value.
     *
     * @return the process exit code (or implementation-defined value if destroyed)
     * @throws ManagedProcessException if the process was never started or start failed
     * @throws ManagedProcessInterruptedException if interrupted while waiting
     */
    int waitForExit() throws ManagedProcessException, ManagedProcessInterruptedException;

    /**
     * Blocks until the process terminates, or until the timeout elapses.
     *
     * @param maxWaitUntilReturning maximum time to wait in milliseconds; a non-negative value
     * @return the process exit code if it terminated within the timeout, or an implementation-defined
     *         sentinel value if still running when the timeout is reached
     * @throws ManagedProcessException if the process was never started or start failed
     * @throws ManagedProcessInterruptedException if interrupted while waiting
     */
    @SuppressWarnings("UnusedReturnValue")
    int waitForExitMaxMs(long maxWaitUntilReturning)
            throws ManagedProcessException, ManagedProcessInterruptedException;

    /**
     * Waits up to the given timeout for the process to terminate; if still running after the timeout,
     * the process is destroyed. Always returns the current instance for chaining.
     *
     * @param maxWaitUntilDestroyTimeout maximum time to wait in milliseconds before destroying
     * @return this managed process instance (for chaining)
     * @throws ManagedProcessException if the process was never started or start failed
     * @throws ManagedProcessInterruptedException if interrupted while waiting
     */
    @SuppressWarnings("UnusedReturnValue")
    ManagedProcess waitForExitMaxMsOrDestroy(long maxWaitUntilDestroyTimeout)
            throws ManagedProcessException, ManagedProcessInterruptedException;

    /**
     * Returns the recent console output captured from the process (stdout and stderr).
     *
     * @return the recent console text (possibly truncated based on configured buffer size), never {@code null}
     */
    String getConsole();

    /**
     * Returns a formatted string with the last N lines of console output for diagnostics.
     *
     * @return a human-readable summary including the most recent console lines
     */
    String getLastConsoleLines();

    /**
     * Indicates whether the watchdog forcibly killed the process.
     *
     * @return {@code true} if the watchdog terminated the process; {@code false} otherwise
     */
    @SuppressWarnings("unused")
    boolean watchDogKilledProcess();

    /**
     * Returns a descriptive name of the process suitable for logs (typically includes the command and,
     * if available, the working directory).
     *
     * @return a descriptive process name for logging
     */
    String getProcLongName();
}
