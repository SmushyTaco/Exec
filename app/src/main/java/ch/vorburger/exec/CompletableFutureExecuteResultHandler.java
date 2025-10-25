/*
 * #%L
 * ch.vorburger.exec
 * %%
 * Copyright (C) 2012 - 2023 Michael Vorburger
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ch.vorburger.exec;

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
