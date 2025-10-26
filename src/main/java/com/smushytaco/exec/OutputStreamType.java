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

/**
 * Represents the two types of output streams produced by a managed process.
 *
 * <p>These correspond to the standard output ({@link #STDOUT}) and standard error
 * ({@link #STDERR}) streams. They are typically used by components such as
 * {@link OutputStreamLogDispatcher} to determine how process output should be logged.
 */
public enum OutputStreamType {
    /**
     * The standard output stream (stdout) of a process.
     *
     * <p>Typically used for informational or regular operational messages.
     */
    STDOUT,
    /**
     * The standard error stream (stderr) of a process.
     *
     * <p>Typically used for error messages or warnings emitted by a process.
     */
    STDERR
}
