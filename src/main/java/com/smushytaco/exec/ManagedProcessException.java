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
