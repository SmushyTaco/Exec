package com.smushytaco.exec;

import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class LoggingShutdownHookProcessDestroyer extends ShutdownHookProcessDestroyer {
    private static final Logger LOG = getLogger(LoggingShutdownHookProcessDestroyer.class);

    @Override
    public void run() {
        LOG.info(
                "Shutdown Hook: JVM is about to exit! Going to kill destroyOnShutdown"
                        + " processes...");
        super.run();
    }
}
