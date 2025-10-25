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

    public ManagedProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagedProcessException(String message) {
        super(message);
    }
}
