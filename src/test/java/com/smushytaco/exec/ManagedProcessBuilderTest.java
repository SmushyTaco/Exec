package com.smushytaco.exec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ManagedProcessBuilder}.
 *
 * @author Michael Vorburger
 */
class ManagedProcessBuilderTest {

    @Test
    void managedProcessBuilder() throws IOException {

        ManagedProcessBuilder mbp =
                new ManagedProcessBuilder(
                        Path.of("/somewhere").resolve("absolute").resolve("bin").resolve("thing"));

        Path arg = Path.of("relative").resolve("file");
        mbp.addArgument(arg);

        // needed to force auto-setting the directory
        mbp.getCommandLine();

        Path cwd = mbp.getWorkingDirectory();
        Assertions.assertNotNull(cwd);
        Path testPath = Path.of("somewhere").resolve("absolute").resolve("bin");
        assertThat(cwd.toAbsolutePath().endsWith(testPath)).isTrue();

        Path execPath = Path.of(mbp.getExecutable());
        Path expectedSuffix = testPath.resolve("thing");

        assertThat(execPath.endsWith(expectedSuffix)).isTrue();

        String arg0 = mbp.getArguments().get(0);
        assertNotSame("relative/file", arg0);
        assertTrue(arg0.contains("relative"));
    }
}
