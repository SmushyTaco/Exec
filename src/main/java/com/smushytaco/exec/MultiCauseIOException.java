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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IOException} that aggregates multiple underlying {@link IOException} causes.
 *
 * <p>This class is useful when multiple I/O operations may fail independently,
 * and you want to capture all of their exceptions together rather than only the first.
 *
 * <p>Each cause can be added using {@link #add(String, IOException)}, and later retrieved via
 * {@link #getCauses()}.
 *
 * @author Michael Vorburger
 */
public class MultiCauseIOException extends IOException {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * Creates a new, initially empty {@code MultiCauseIOException}.
     *
     * <p>Call {@link #add(String, IOException)} at least once to record
     * individual I/O errors before throwing this exception.
     */
    public MultiCauseIOException() {}

    /**
     * The list of underlying {@link IOException} causes aggregated by this exception.
     *
     * <p>Each cause includes its own message and stack trace. This list is populated by calls
     * to {@link #add(String, IOException)} and may contain multiple entries if several
     * operations failed during execution.
     */
    protected final ArrayList<IOException> causes = new ArrayList<>();

    /**
     * Add a Cause. Must be called at least once! (Otherwise why use this.)
     *
     * @param message like the constructor argument of a Throwable
     * @param cause like the constructor argument of a Throwable
     */
    public void add(String message, IOException cause) {
        causes.add(new IOException(message, cause));
    }

    /**
     * Returns the list of all underlying {@link IOException} instances that caused this exception.
     *
     * @return a mutable list containing the individual causes in the order they were added
     */
    @SuppressWarnings("unused")
    public List<IOException> getCauses() {
        return causes;
    }

    @Override
    public String getMessage() {
        // checkUsage();
        StringBuilder message = new StringBuilder("MultiCauseIOException, causes: ");
        for (int i = 0; i < causes.size(); i++) {
            message.append(i);
            message.append(". ");
            message.append(causes.get(i).getMessage());
        }
        return message.toString();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        s.println("MultiCauseIOException, stack traces: ");
        for (int i = 0; i < causes.size(); i++) {
            s.print(i);
            s.print(". ");
            causes.get(i).printStackTrace(s);
        }
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        s.println("MultiCauseIOException, stack traces: ");
        for (int i = 0; i < causes.size(); i++) {
            s.print(i);
            s.print(". ");
            causes.get(i).printStackTrace(s);
        }
    }

    @Override
    public synchronized Throwable getCause() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        throw new UnsupportedOperationException();
    }
}
