package com.smushytaco.exec;

import org.apache.commons.exec.LogOutputStream;

/**
 * Rolling Process Output Buffer.
 *
 * @author Michael Vorburger
 */
// intentionally package local for now
class RollingLogOutputStream extends LogOutputStream {

    private final CircularFifoQueue<String> ringBuffer;

    RollingLogOutputStream(int maxLines) {
        ringBuffer = new CircularFifoQueue<>(maxLines);
    }

    @Override
    protected synchronized void processLine(String line, @SuppressWarnings("unused") int level) {
        ringBuffer.add(line);
    }

    /**
     * Returns recent lines (up to maxLines from constructor).
     *
     * <p>The implementation is relatively expensive here; the design is intended for many
     * processLine() calls and few getRecentLines().
     *
     * @return recent Console output
     */
    public synchronized String getRecentLines() {
        StringBuilder sb = new StringBuilder();
        for (String line : ringBuffer) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(line);
        }
        return sb.toString();
    }
}
