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

import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of {@link ShutdownHookProcessDestroyer} that logs when the JVM is shutting down
 * and about to destroy registered processes.
 *
 * <p>This class behaves exactly like the base implementation, except that it emits a log message
 * via SLF4J before delegating to {@link ShutdownHookProcessDestroyer#run()}.
 *
 * <p>It is used by {@link ManagedProcess} when {@link ManagedProcessBuilder#isDestroyOnShutdown()} is enabled to ensure
 * graceful cleanup and clear visibility in logs whenever the JVM terminates while managed
 * processes are still running.
 *
 * @see ManagedProcess
 * @see ManagedProcessBuilder
 * @see ShutdownHookProcessDestroyer
 */
public class LoggingShutdownHookProcessDestroyer extends ShutdownHookProcessDestroyer {
    private static final Logger logger = LoggerFactory.getLogger(LoggingShutdownHookProcessDestroyer.class);
    /**
     * Creates a new instance of this process destroyer.
     *
     * <p>This constructor simply delegates to the superclass constructor and registers
     * no processes initially.
     */
    public LoggingShutdownHookProcessDestroyer() {
        super();
    }
    @Override
    public void run() {
        logger.info(
                "Shutdown Hook: JVM is about to exit! Going to kill destroyOnShutdown"
                        + " processes...");
        super.run();
    }
}
