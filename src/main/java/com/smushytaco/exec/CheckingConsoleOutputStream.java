package com.smushytaco.exec;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * OutputStream which watches out for the occurrence of a keyword (String).
 *
 * <p>Used to watch the console output of a {@link ManagedProcess} for a matching String.
 *
 * @author Michael Vorburger
 */
public final class CheckingConsoleOutputStream extends OutputStream {
    private final CharsetDecoder dec;
    private final @Nullable Function<String, @Nullable String> onMatchNext;
    private final AtomicBoolean done = new AtomicBoolean(false);

    private String literal;
    private Kmp kmp;

    private final CharBuffer cbuf = CharBuffer.allocate(4096);

    @SuppressWarnings("IdentifierName")
    private boolean pendingCR = false;

    public CheckingConsoleOutputStream(
            String literal,
            @Nullable Function<String, @Nullable String> onMatchNext,
            @Nullable Charset cs) {
        if (literal.isEmpty()) {
            throw new IllegalArgumentException("literal must not be empty");
        }
        this.literal = normalizeNl(literal);
        this.onMatchNext = onMatchNext;
        this.dec =
                (cs == null ? Charset.defaultCharset() : cs)
                        .newDecoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE);
        this.kmp = new Kmp(this.literal);
    }

    public boolean hasSeen() {
        return done.get();
    }

    @Override
    public synchronized void write(int b) throws IOException {
        write(new byte[] {(byte) b}, 0, 1);
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
            drainBuffer();
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
        CoderResult cr = dec.decode(ByteBuffer.allocate(0), cbuf, true);
        if (cr.isError()) {
            cr.throwException();
        }
        drainBuffer();
        cr = dec.flush(cbuf);
        if (cr.isError()) {
            cr.throwException();
        }
        drainBuffer();
        if (pendingCR && !hasSeen() && kmp.accept('\n')) {
            onMatched();
        }
    }

    private void drainBuffer() {
        cbuf.flip();
        while (!hasSeen() && cbuf.hasRemaining()) {
            char c = cbuf.get();
            if (pendingCR) {
                if (kmp.accept('\n')) {
                    onMatched();
                }
                pendingCR = false;
                if (c == '\n') {
                    continue;
                }
            }
            if (c == '\r') {
                if (cbuf.hasRemaining() && cbuf.get(cbuf.position()) == '\n') {
                    cbuf.get();
                    if (kmp.accept('\n')) {
                        onMatched();
                    }
                } else {
                    pendingCR = true;
                }
                continue;
            }
            if (kmp.accept(c)) {
                onMatched();
            }
        }
        cbuf.clear();
    }

    private void onMatched() {
        if (onMatchNext == null) {
            done.set(true);
            return;
        }
        String next = onMatchNext.apply(literal);
        if (next == null) {
            done.set(true);
            return;
        }
        String nextNorm = normalizeNl(next);
        if (!Objects.equals(nextNorm, this.literal)) {
            this.literal = nextNorm;
            this.kmp = new Kmp(this.literal);
        } else {
            this.kmp.reset();
        }
    }

    private static String normalizeNl(String s) {
        return s.replace("\r\n", "\n").replace("\r", "\n");
    }

    private static final class Kmp {
        private final char[] p;
        private final int[] lps;
        private int j = 0;

        Kmp(String pat) {
            this.p = pat.toCharArray();
            this.lps = buildLps(p);
        }

        boolean accept(char c) {
            while (j > 0 && c != p[j]) {
                j = lps[j - 1];
            }
            if (c == p[j]) {
                if (++j == p.length) {
                    j = lps[j - 1];
                    return true;
                }
            }
            return false;
        }

        void reset() {
            j = 0;
        }

        private static int[] buildLps(char[] p) {
            int[] lps = new int[p.length];
            int len = 0;
            for (int i = 1; i < p.length; i++) {
                while (len > 0 && p[i] != p[len]) {
                    len = lps[len - 1];
                }
                if (p[i] == p[len]) {
                    len++;
                }
                lps[i] = len;
            }
            return lps;
        }
    }
}
