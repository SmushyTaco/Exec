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
