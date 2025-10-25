package com.smushytaco.exec;

import org.jspecify.annotations.Nullable;

/**
 * Listener interface users can implement to get notified about process completion/failure.
 *
 * @author Neelesh Shastry
 */
public interface ManagedProcessListener {

    void onProcessComplete(int exitValue);

    void onProcessFailed(int exitValue, @Nullable Throwable throwable);
}
