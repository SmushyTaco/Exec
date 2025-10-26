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
