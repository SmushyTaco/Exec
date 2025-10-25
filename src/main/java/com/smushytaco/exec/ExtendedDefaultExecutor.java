package com.smushytaco.exec;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.function.IntPredicate;

class ExtendedDefaultExecutor extends DefaultExecutor {

    private @Nullable IntPredicate exitValueChecker;

    ExtendedDefaultExecutor(@Nullable Path workingDirectory) {
        super(workingDirectory, Executors.defaultThreadFactory(), new PumpStreamHandler());
    }

    void setIsSuccessExitValueChecker(@Nullable IntPredicate exitValueChecker) {
        this.exitValueChecker = exitValueChecker;
    }

    @Override
    public boolean isFailure(int exitValue) {
        if (exitValueChecker == null) {
            return super.isFailure(exitValue);
        } else {
            return !exitValueChecker.test(exitValue);
        }
    }
}
