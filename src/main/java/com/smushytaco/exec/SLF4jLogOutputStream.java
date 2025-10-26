package com.smushytaco.exec;

import org.apache.commons.exec.LogOutputStream;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * OutputStream which logs to SLF4j.
 *
 * <p>With many thanks to <a
 * href="https://stackoverflow.com/questions/5499042/writing-output-error-to-log-files-using">PumpStreamHandler</a>
 *
 * @author Michael Vorburger
 */
class SLF4jLogOutputStream extends LogOutputStream {

    private final OutputStreamLogDispatcher dispatcher;
    private final Logger logger;
    private final OutputStreamType type;
    private final String pid;

    protected SLF4jLogOutputStream(
            Logger logger,
            String pid,
            OutputStreamType type,
            OutputStreamLogDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.logger = logger;
        this.type = type;
        this.pid = pid;
    }

    @Override
    protected void processLine(String line, @SuppressWarnings("unused") int level) {
        Level logLevel = dispatcher.dispatch(type, line);
        if (logLevel == null) {
            return;
        }
        switch (logLevel) {
            case TRACE -> logger.trace("{}: {}", pid, line);
            case DEBUG -> logger.debug("{}: {}", pid, line);
            case INFO -> logger.info("{}: {}", pid, line);
            case WARN -> logger.warn("{}: {}", pid, line);
            case ERROR -> logger.error("{}: {}", pid, line);
        }
    }
}
