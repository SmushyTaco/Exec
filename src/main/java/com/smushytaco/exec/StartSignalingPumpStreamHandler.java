package com.smushytaco.exec;

import org.apache.commons.exec.PumpStreamHandler;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

class StartSignalingPumpStreamHandler extends PumpStreamHandler {
    private final CountDownLatch started;

    StartSignalingPumpStreamHandler(
            OutputStream outputStream,
            OutputStream errorOutputStream,
            @Nullable InputStream inputStream,
            CountDownLatch started) {
        super(outputStream, errorOutputStream, inputStream);
        this.started = started;
    }

    @Override
    public void start() {
        super.start();
        started.countDown();
    }
}
