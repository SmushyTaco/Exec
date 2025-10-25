package com.smushytaco.exec;

import java.io.IOException;

public interface ManagedProcessState {

    @SuppressWarnings("UnusedReturnValue")
    boolean startAndWaitForConsoleMessageMaxMs(String messageInConsole, long maxWaitUntilReturning)
            throws IOException, ManagedProcessInterruptedException;

    void destroy() throws ManagedProcessException, ManagedProcessInterruptedException;

    boolean isAlive();

    void notifyProcessHalted();

    @SuppressWarnings("UnusedReturnValue")
    int exitValue() throws ManagedProcessException, ManagedProcessInterruptedException;

    int waitForExit() throws ManagedProcessException, ManagedProcessInterruptedException;

    @SuppressWarnings("UnusedReturnValue")
    int waitForExitMaxMs(long maxWaitUntilReturning)
            throws ManagedProcessException, ManagedProcessInterruptedException;

    @SuppressWarnings("UnusedReturnValue")
    ManagedProcess waitForExitMaxMsOrDestroy(long maxWaitUntilDestroyTimeout)
            throws ManagedProcessException, ManagedProcessInterruptedException;

    String getConsole();

    String getLastConsoleLines();

    @SuppressWarnings("unused")
    boolean watchDogKilledProcess();

    String getProcLongName();
}
