# Exec
[![Maven Central](https://img.shields.io/maven-central/v/com.smushytaco/exec.svg?label=maven%20central)](https://central.sonatype.com/artifact/com.smushytaco/exec)
[![Javadocs](https://javadoc.io/badge2/com.smushytaco/exec/javadoc.svg)](https://javadoc.io/doc/com.smushytaco/exec)

This is a small library allowing to launch external processes from Java code in the background,
and correctly pipe their output e.g. into SLF4J, await either their termination or specific output, etc.

Usage
---

Launching external processes from Java using the raw java.lang.ProcessBuilder API directly can be a little cumbersome.
[Apache Commons Exec](https://commons.apache.org/proper/commons-exec/) makes it a bit easier, but lacks some convenience.
This library makes it truly convenient:

```java
ManagedProcessBuilder pb = new ManagedProcessBuilder("someExec")
    .addArgument("arg1")
    .setWorkingDirectory(Path.of("/tmp"))
    .getEnvironment().put("ENV_VAR", "...")
    .setDestroyOnShutdown(true)
    .addStdOut(new BufferedOutputStream(new FileOutputStream(outputFile)))
    .setConsoleBufferMaxLines(7000);  // used by startAndWaitForConsoleMessageMaxMs

ManagedProcess p = pb.build();
p.start();
p.isAlive();
p.waitForExit();
// OR: p.waitForExitMaxMsOrDestroy(5000);
// OR: p.startAndWaitForConsoleMessageMaxMs("Successfully started", 3000);
p.exitValue();
// OR: p.destroy();

// This works even while it's running, not just when it exited
String output = p.getConsole();
```

If you need to, you can also attach a listener to get notified when the external process ends, by using `setProcessListener()` on the `ManagedProcessBuilder` with a `ManagedProcessListener` that implements `onProcessComplete()` and `onProcessFailed()`.

We currently internally use Apache Commons Exec by building on top, extending and wrapping it,
but without exposing this in its API, so that theoretically in the future this implementation detail could be changed.

Advantages
---

* automatically logs external process's STDOUT and STDERR using SLF4j out of the box (can be customized)
* automatically logs and throws for common errors (e.g. executable not found), instead of silently ignoring like j.l.Process
* automatically destroys external process with JVM shutdown hook (can be disabled)
* lets you await appearance of certain messages on the console
* lets you write tests against the expected output

History
---

Historically, this code was part of [MariaDB4j](https://github.com/vorburger/MariaDB4j/) (and this is why it's initial version was 3.0.0),
but was it later split into a separate project. This was done to make it usable in separate projects. It was then later forked
and modernized by me (Nikan Radan, also known as SmushyTaco).
