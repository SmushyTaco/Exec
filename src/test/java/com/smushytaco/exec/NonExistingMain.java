package com.smushytaco.exec;

// https://github.com/vorburger/ch.vorburger.exec/issues/9
public final class NonExistingMain {

    public static void main(String[] args)
            throws ManagedProcessException, ManagedProcessInterruptedException {
        ManagedProcessBuilder mpb = new ManagedProcessBuilder("cmd-does-not-exist");
        ManagedProcess p = mpb.build();
        p.start();
    }

    private NonExistingMain() {}
}
