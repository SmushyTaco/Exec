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
