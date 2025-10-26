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
import java.io.Serial;

/**
 * Exception thrown when unexpected stuff happens in ManagedProcess.
 *
 * @author Michael Vorburger
 */
public class ManagedProcessException extends IOException {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ManagedProcessException} with the specified detail message
     * and underlying cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of this exception
     */
    public ManagedProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code ManagedProcessException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ManagedProcessException(String message) {
        super(message);
    }
}
