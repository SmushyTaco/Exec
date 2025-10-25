package com.smushytaco.exec;

import java.io.Serial;

public class ManagedProcessInterruptedException extends InterruptedException {
    @Serial private static final long serialVersionUID = 1L;

    private final String where;
    private final String procLongName;

    public ManagedProcessInterruptedException(String where, String procLongName) {
        super("Interrupted at \"" + where + "\": " + procLongName);
        this.where = where;
        this.procLongName = procLongName;
    }

    @SuppressWarnings("unused")
    public String getWhere() {
        return where;
    }

    @SuppressWarnings("unused")
    public String getProcLongName() {
        return procLongName;
    }

    public static ManagedProcessInterruptedException withCause(
            String where, String procLongName, InterruptedException cause) {
        ManagedProcessInterruptedException exception =
                new ManagedProcessInterruptedException(where, procLongName);
        exception.initCause(cause);
        return exception;
    }
}
