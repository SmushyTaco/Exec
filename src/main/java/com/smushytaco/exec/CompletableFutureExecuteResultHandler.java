package com.smushytaco.exec;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("ClassCanBeRecord")
class CompletableFutureExecuteResultHandler implements ExecuteResultHandler {

    private final CompletableFuture<Integer> asyncResult;
    private final @Nullable ManagedProcessListener listener;
    private final ManagedProcess owner;

    public CompletableFutureExecuteResultHandler(CompletableFuture<Integer> asyncResult,
        @Nullable ManagedProcessListener listener, ManagedProcess owner) {
        this.asyncResult = asyncResult;
        this.listener = listener;
        this.owner = owner;
    }

    /**
     * The asynchronous execution completed.
     *
     * @param exitValue the exit value of the sub-process
     */
    @Override
    public void onProcessComplete(int exitValue) {
        if (listener != null) {
            listener.onProcessComplete(exitValue);
        }
        asyncResult.complete(exitValue);
    }

    /**
     * The asynchronous execution failed.
     *
     * @param e the {@code ExecuteException} containing the root cause
     */
    @Override
    public void onProcessFailed(ExecuteException e) {
        if (listener != null) {
            listener.onProcessFailed(e.getExitValue(), e);
        }
        asyncResult.completeExceptionally(e);
        owner.notifyProcessHalted();
    }
}
