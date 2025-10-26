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

    /**
     * The list of underlying {@link OutputStream} instances that this multiplexer delegates to.
     *
     * <p>Each stream receives all write, flush, and close operations in the order they were added.
     * If any stream throws an {@link IOException}, it is recorded but does not prevent other
     * streams from being written to.
     */
    protected final List<OutputStream> streams = new ArrayList<>();

    /**
     * Creates an empty {@code MultiOutputStream} with no delegate streams.
     *
     * <p>Streams can be added later using {@link #addOutputStream(OutputStream)}.
     */
    public MultiOutputStream() {}

    /**
     * Creates a {@code MultiOutputStream} that delegates to the given {@link OutputStream}s.
     *
     * @param delegates one or more output streams that will receive all written data
     */
    @SuppressWarnings("unused")
    public MultiOutputStream(OutputStream... delegates) {
        streams.addAll(Arrays.asList(delegates));
    }

    /**
     * Adds an {@link OutputStream} to this multiplexer.
     *
     * <p>All future writes will be forwarded to this stream as well.
     *
     * @param delegate the {@link OutputStream} to add
     * @return this {@code MultiOutputStream} instance for chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    public synchronized MultiOutputStream addOutputStream(OutputStream delegate) {
        streams.add(delegate);
        return this;
    }

    /**
     * Removes an {@link OutputStream} from this multiplexer.
     *
     * <p>After removal, the stream will no longer receive any writes or flush calls.
     *
     * @param delegate the {@link OutputStream} to remove
     * @return this {@code MultiOutputStream} instance for chaining
     */
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
