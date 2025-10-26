package com.smushytaco.exec;


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OutputStream "Multiplexer" which delegates to a list of other registered OutputStreams.
 *
 * <p>It's kinda like UNIX "tee". Forwarding is in the order the delegates are added. The
 * implementation is synchronous, so the added OutputStreams should be "fast" in order not to block
 * each other.
 *
 * <p>Exceptions thrown by added OutputStreams are handled gracefully: They at first do not prevent
 * delegating to the other registered OutputStreams, but then are rethrown after we've pushed to
 * delegates (possibly containing multiple causes).
 *
 * @author Michael Vorburger
 */
public class MultiOutputStream extends OutputStream {

    protected final List<OutputStream> streams = new ArrayList<>();

    public MultiOutputStream() {}

    @SuppressWarnings("unused")
    public MultiOutputStream(OutputStream... delegates) {
        streams.addAll(Arrays.asList(delegates));
    }

    @SuppressWarnings("UnusedReturnValue")
    public synchronized MultiOutputStream addOutputStream(OutputStream delegate) {
        streams.add(delegate);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public synchronized MultiOutputStream removeOutputStream(OutputStream delegate) {
        streams.remove(delegate);
        return this;
    }

    @Override
    public void write(int b) throws IOException {
        MultiCauseIOException mex = null;
        for (OutputStream stream : streams) {
            try {
                stream.write(b);
            } catch (IOException e) {
                if (mex == null) {
                    mex = new MultiCauseIOException();
                }
                mex.add("MultiOutputStream write(int b) delegation failed", e);
            }
        }
        if (mex != null) {
            throw mex;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        MultiCauseIOException mex = null;
        for (OutputStream stream : streams) {
            try {
                stream.write(b);
            } catch (IOException e) {
                if (mex == null) {
                    mex = new MultiCauseIOException();
                }
                mex.add("MultiOutputStream write(byte[] b) delegation failed", e);
            }
        }
        if (mex != null) {
            throw mex;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        MultiCauseIOException mex = null;
        for (OutputStream stream : streams) {
            try {
                stream.write(b, off, len);
            } catch (IOException e) {
                if (mex == null) {
                    mex = new MultiCauseIOException();
                }
                mex.add("MultiOutputStream write(byte[] b, int off, int len) delegation failed", e);
            }
        }
        if (mex != null) {
            throw mex;
        }
    }

    @Override
    public void flush() throws IOException {
        MultiCauseIOException mex = null;
        for (OutputStream stream : streams) {
            try {
                stream.flush();
            } catch (IOException e) {
                if (mex == null) {
                    mex = new MultiCauseIOException();
                }
                mex.add("MultiOutputStream flush() delegation failed", e);
            }
        }
        if (mex != null) {
            throw mex;
        }
    }

    @Override
    public void close() throws IOException {
        MultiCauseIOException mex = null;
        for (OutputStream stream : streams) {
            try {
                stream.close();
            } catch (IOException e) {
                if (mex == null) {
                    mex = new MultiCauseIOException();
                }
                mex.add("MultiOutputStream close() delegation failed", e);
            }
        }
        if (mex != null) {
            throw mex;
        }
    }
}
