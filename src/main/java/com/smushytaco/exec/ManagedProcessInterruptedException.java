package com.smushytaco.exec;

import java.io.Serial;

/**
 * Exception indicating that a {@link ManagedProcess} was interrupted while waiting or executing.
 *
 * <p>This wraps an {@link InterruptedException} to provide additional context
 * such as where in the process lifecycle the interruption occurred and which
 * process was involved.
 *
 * @author Nikan Radan
 */
public class ManagedProcessInterruptedException extends InterruptedException {
    @Serial private static final long serialVersionUID = 1L;

    /** The logical location (e.g., method or phase) where the interruption occurred. */
    private final String where;
    /** The descriptive long name of the managed process that was interrupted. */
    private final String procLongName;

    /**
     * Creates a new {@code ManagedProcessInterruptedException} describing where and for which
     * process the interruption occurred.
     *
     * @param where a short description of where the interruption happened (e.g. "startExecute")
     * @param procLongName the descriptive name of the managed process
     */
    public ManagedProcessInterruptedException(String where, String procLongName) {
        super("Interrupted at \"" + where + "\": " + procLongName);
        this.where = where;
        this.procLongName = procLongName;
    }

    /**
     * Returns the logical section or method name where the interruption occurred.
     *
     * @return a short identifier of where the interruption happened
     */
    @SuppressWarnings("unused")
    public String getWhere() {
        return where;
    }

    /**
     * Returns the descriptive name of the managed process that was interrupted.
     *
     * @return the process's long name
     */
    @SuppressWarnings("unused")
    public String getProcLongName() {
        return procLongName;
    }

    /**
     * Creates a new {@code ManagedProcessInterruptedException} with an underlying cause.
     *
     * <p>This is a convenience method for wrapping an existing {@link InterruptedException}
     * while preserving its cause chain.
     *
     * @param where where the interruption occurred
     * @param procLongName the descriptive name of the managed process
     * @param cause the original {@link InterruptedException} that triggered this
     * @return a new {@code ManagedProcessInterruptedException} initialized with the given cause
     */
    public static ManagedProcessInterruptedException withCause(String where, String procLongName, InterruptedException cause) {
        ManagedProcessInterruptedException exception =
                new ManagedProcessInterruptedException(where, procLongName);
        exception.initCause(cause);
        return exception;
    }
}
