/*
 * #%L
 * ch.vorburger.exec
 * %%
 * Copyright (C) 2012 - 2023 Michael Vorburger
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ch.vorburger.exec;

import com.google.errorprone.annotations.Var;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OutputStream which watches out for the occurrence of a keyword (String).
 *
 * <p>Used to watch the console output of a {@link ManagedProcess} for a matching String.
 *
 * @author Michael Vorburger
 */
public final class CheckingConsoleOutputStream extends OutputStream {
    private final CharsetDecoder dec;
    private final @Nullable Runnable onSeen;
    private final AtomicBoolean seen = new AtomicBoolean(false);
    private final Kmp kmp;
    private final CharBuffer cbuf = CharBuffer.allocate(4096);

    public CheckingConsoleOutputStream(String literal, @Nullable Runnable onSeen, @Nullable Charset cs) {
        if (literal.isEmpty()) {
            throw new IllegalArgumentException("literal must not be empty");
        }
        this.dec = (cs == null ? Charset.defaultCharset() : cs)
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        this.kmp = new Kmp(literal.replace("\r\n", "\n").replace("\r", "\n"));
        this.onSeen = onSeen;
    }

    public boolean hasSeen() { return seen.get(); }

    @Override
    public synchronized void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (hasSeen()) {
            return;
        }
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }

        ByteBuffer in = ByteBuffer.wrap(b, off, len);
        while (in.hasRemaining()) {
            CoderResult cr = dec.decode(in, cbuf, false);
            if (cr.isError()) {
                cr.throwException();
            }
            drainBuffer(); // Line 83
            if (hasSeen()) {
                return;
            }
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (hasSeen()) {
            return;
        }
        @Var CoderResult cr = dec.decode(ByteBuffer.allocate(0), cbuf, true);
        if (cr.isError()) {
            cr.throwException();
        }
        drainBuffer();
        cr = dec.flush(cbuf);
        if (cr.isError()) {
            cr.throwException();
        }
        drainBuffer();
    }

    @SuppressWarnings("IdentifierName")
    private boolean pendingCR = false;
    private void drainBuffer() {
        cbuf.flip();
        while (!hasSeen() && cbuf.hasRemaining()) {
            char c = cbuf.get();
            if (pendingCR) {
                if (kmp.accept('\n')) {
                    fireOnce();
                }
                pendingCR = false;
                if (c == '\n') {
                    continue;
                }
            }
            if (c == '\r') {
                if (cbuf.hasRemaining() && cbuf.get(cbuf.position()) == '\n') { // Line 123
                    cbuf.get();
                    if (kmp.accept('\n')) {
                        fireOnce();
                    }
                } else {
                    pendingCR = true;
                }
                continue;
            }
            if (kmp.accept(c)) {
                fireOnce();
            }
        }
        cbuf.clear();
    }


    private void fireOnce() {
        if (seen.compareAndSet(false, true) && onSeen != null) {
            onSeen.run();
        }
    }

    /** Minimal KMP for streaming literal match. */
    private static final class Kmp {
        private final char[] p;
        private final int[] lps;
        private int j = 0;

        Kmp(String pat) {
            this.p = pat.toCharArray();
            this.lps = buildLps(p);
        }

        /** Feed one char; returns true if full pattern just matched. */
        boolean accept(char c) {
            while (j > 0 && c != p[j]) {
                j = lps[j - 1];
            }
            return c == p[j] && ++j == p.length;
        }

        private static int[] buildLps(char[] p) {
            int[] lps = new int[p.length];
            @Var int len = 0;
            for (int i = 1; i < p.length; i++) {
                while (len > 0 && p[i] != p[len]) {
                    len = lps[len - 1];
                }
                if (p[i] == p[len]) {
                    len++;
                    lps[i] = len;
                } else {
                    lps[i] = 0;
                }
            }
            return lps;
        }
    }
}
