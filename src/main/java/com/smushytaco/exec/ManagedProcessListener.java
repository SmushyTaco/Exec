package com.smushytaco.exec;

import org.jspecify.annotations.Nullable;

/**
 * Listener interface users can implement to get notified about process completion/failure.
 *
 * @author Neelesh Shastry
 */
public interface ManagedProcessListener {

    /**
     * Called when a managed process terminates successfully.
     *
     * @param exitValue the exit code returned by the process (typically {@code 0} for success)
     */
    void onProcessComplete(int exitValue);

    /**
     * Called when a managed process terminates abnormally or encounters an error.
     *
     * @param exitValue the exit code returned by the process (non-zero indicates failure)
     * @param throwable an optional exception describing the failure cause, or {@code null}
     *                  if the failure reason is unknown
     */
    void onProcessFailed(int exitValue, @Nullable Throwable throwable);
}
