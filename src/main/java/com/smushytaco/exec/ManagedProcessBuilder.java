package com.smushytaco.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.exec.util.StringUtils;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

/**
 * Builder for ManagedProcess.
 *
 * <p>This is inspired by {@link java.lang.ProcessBuilder} &amp; {@link CommandLine}, and/but:
 *
 * <p>It offers to add java.nio.Path arguments, and makes sure that their absolute path is used.
 *
 * <p>If no directory is set, it automatically sets the initial working directory using the
 * directory of executable if it was a Path, and thus makes sure an initial working directory is
 * always passed to the process.
 *
 * <p>It intentionally doesn't offer "parsing" space delimited command "lines", but forces you to
 * set an executable and add arguments.
 *
 * @author Michael Vorburger
 * @author Neelesh Shastry
 * @author William Dutton
 */
public class ManagedProcessBuilder {

    /** Underlying Apache Commons Exec command line being built. */
    protected final CommandLine commonsExecCommandLine;
    /** Environment variables to pass to the launched process. */
    protected final Map<String, String> environment;
    /** Working directory for the launched process, or {@code null} to use the default. */
    protected @Nullable Path directory;
    /** Optional stdin stream that will be piped to the launched process. */
    protected @Nullable InputStream inputStream;
    /** If {@code true}, register a shutdown hook to destroy the process on JVM exit. */
    protected boolean destroyOnShutdown = true;
    /** Number of recent console lines to retain in memory for diagnostics/logging. */
    protected int consoleBufferMaxLines = 100;
    /**
     * Strategy used to decide how each line from STDOUT/STDERR should be logged.
     * Consulted by the SLF4J output sink to map a line to an SLF4J {@link org.slf4j.event.Level}
     * (or to suppress logging by returning {@code null}). The default implementation logs
     * STDOUT at INFO and STDERR at ERROR.
     */
    protected OutputStreamLogDispatcher outputStreamLogDispatcher = new OutputStreamLogDispatcher();
    /** Optional listener notified on process lifecycle events. */
    protected @Nullable ManagedProcessListener listener;
    /** Additional output streams to receive the process STDOUT. */
    protected List<OutputStream> stdOuts = new ArrayList<>();
    /** Additional output streams to receive the process STDERR. */
    protected List<OutputStream> stdErrs = new ArrayList<>();
    /**
     * Predicate that decides whether an exit value represents success.
     * Defaults to {@code exitValue == 0}.
     */
    protected IntPredicate isSuccessExitValueChecker = exitValue -> exitValue == 0;

    /**
     * Returns the currently configured process listener.
     *
     * @return the listener, or {@code null} if none is set
     */
    @SuppressWarnings("unused")
    public @Nullable ManagedProcessListener getProcessListener() {
        return listener;
    }

    /**
     * Sets the process listener to be notified of lifecycle events.
     *
     * @param listener the listener to use, or {@code null} to clear
     * @return this builder instance for chaining
     */
    public ManagedProcessBuilder setProcessListener(@Nullable ManagedProcessListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Creates a builder for the given executable path (string form).
     *
     * @param executable the executable to run (absolute or resolvable on PATH)
     * @throws ManagedProcessException if the environment cannot be initialized
     */
    public ManagedProcessBuilder(String executable) throws ManagedProcessException {
        commonsExecCommandLine = new CommandLine(executable);
        environment = initialEnvironment();
    }

    /**
     * Creates a builder for the given executable path.
     *
     * @param executable the executable to run
     * @throws ManagedProcessException if the environment cannot be initialized
     */
    public ManagedProcessBuilder(Path executable) throws ManagedProcessException {
        commonsExecCommandLine = new CommandLine(executable);
        environment = initialEnvironment();
    }

    /**
     * Initializes the default environment for a new process.
     * <p>Static to avoid leaking {@code this} during subclass construction.</p>
     *
     * @return a mutable map of environment variables
     * @throws ManagedProcessException if the environment cannot be retrieved
     */
    protected static Map<String, String> initialEnvironment() throws ManagedProcessException {
        try {
            return EnvironmentUtils.getProcEnvironment();
        } catch (IOException e) {
            throw new ManagedProcessException("Retrieving default environment variables failed", e);
        }
    }

    /**
     * Adds a raw argument to the command.
     *
     * @param arg the argument text
     * @param handleQuoting if {@code true}, escape/quote as needed
     * @return this builder instance for chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    public ManagedProcessBuilder addArgument(String arg, boolean handleQuoting) {
        commonsExecCommandLine.addArgument(arg, handleQuoting);
        return this;
    }

    /**
     * Adds a Path as an argument to the command. This uses {@link Path#toRealPath(LinkOption...)},
     * which is usually what you'll actually want when launching external processes.
     *
     * @param arg the Path to add
     * @return this
     * @throws IOException if {@link Path#toRealPath(LinkOption...)} and the {@link
     *     File#getCanonicalPath()} fallback both fail.
     * @see ProcessBuilder
     */
    @SuppressWarnings("UnusedReturnValue")
    public ManagedProcessBuilder addArgument(Path arg) throws IOException {
        String canonical;
        try {
            canonical = arg.toRealPath().toString();
        } catch (IOException e) {
            canonical = arg.toFile().getCanonicalPath();
        }

        addArgument(canonical, true);
        return this;
    }

    /**
     * Adds an argument to the command.
     *
     * @param arg the String Argument to add. It will be escaped with single or double quote if it
     *     contains a space.
     * @return this
     * @see ProcessBuilder
     */
    public ManagedProcessBuilder addArgument(String arg) {
        addArgument(arg, true);
        return this;
    }

    /**
     * Adds a single argument to the command, composed of two parts. The two parts are independently
     * escaped (see {@link #addArgument(String)}), and then concatenated without any separator.
     *
     * @param argPart1 the first part of the argument
     * @param argPart2 the second part of the argument
     * @return this builder instance for chaining
     */
    @SuppressWarnings("unused")
    public ManagedProcessBuilder addArgument(String argPart1, String argPart2) {
        addArgument(argPart1, "", argPart2); // No separator
        return this;
    }

    /**
     * Adds a single argument to the command, composed of two parts joined by a separator. Each part
     * is escaped independently and then concatenated using the given separator.
     *
     * <p>This overload allows precise control over the joining character (for example, {@code "="}
     * for arguments like {@code --config=file.txt}).
     *
     * @param argPart1 the first part of the argument
     * @param separator the separator string to place between the two parts
     * @param argPart2 the second part of the argument
     * @return this builder instance for chaining
     */
    protected ManagedProcessBuilder addArgument(String argPart1, String separator, String argPart2) {
        StringBuilder sb = new StringBuilder();
        String arg;

        if (isWindows()) {
            sb.append(argPart1);
            sb.append(separator);
            sb.append(argPart2);
            arg = StringUtils.quoteArgument(sb.toString());
        } else {
            sb.append(StringUtils.quoteArgument(argPart1));
            sb.append(separator);
            sb.append(StringUtils.quoteArgument(argPart2));
            arg = sb.toString();
        }
        addArgument(arg, false);

        return this;
    }

    /**
     * Adds a single argument to the command, composed of a prefix and a file path separated by
     * {@code "="}. Both the prefix and path are escaped independently before concatenation.
     *
     * @param arg the argument prefix, such as a flag name (for example {@code "--config"})
     * @param path the file path to append to the argument
     * @return this builder instance for chaining
     * @throws IOException if canonical or real path resolution fails
     */
    @SuppressWarnings("unused")
    public ManagedProcessBuilder addFileArgument(String arg, Path path) throws IOException {
        String canonical;
        try {
            canonical = path.toRealPath().toString();
        } catch (IOException e) {
            canonical = path.toFile().getCanonicalPath();
        }
        return addArgument(arg, "=", canonical);
    }

    /**
     * Returns the current list of arguments (after quoting/escaping).
     *
     * @return immutable list of argument strings
     */
    public List<String> getArguments() {
        return List.of(commonsExecCommandLine.getArguments());
    }

    /**
     * Sets working directory.
     *
     * @param directory working directory to use for process to be launched
     * @return this
     * @see ProcessBuilder#directory(java.io.File)
     */
    @SuppressWarnings("UnusedReturnValue")
    public ManagedProcessBuilder setWorkingDirectory(@Nullable Path directory) {
        this.directory = directory;
        return this;
    }

    /**
     * Returns the working directory that will be used for the launched process.
     *
     * @return the working directory, or {@code null} if none has been set
     * @see ProcessBuilder#directory()
     */
    public @Nullable Path getWorkingDirectory() {
        return directory;
    }

    /**
     * Returns the environment variables that will be passed to the launched process.
     *
     * <p>The returned map is live—changes to it will affect subsequently launched
     * processes built from this builder.
     *
     * @return the map of environment variables
     */
    @SuppressWarnings("unused")
    public Map<String, String> getEnvironment() {
        return environment;
    }

    /**
     * Returns the executable path string that will be launched.
     *
     * @return the executable as a string
     */
    public String getExecutable() {
        return commonsExecCommandLine.getExecutable();
    }

    /**
     * Returns whether the process will be destroyed automatically on JVM shutdown.
     *
     * @return {@code true} if destroy-on-shutdown is enabled
     */
    @SuppressWarnings("unused")
    public boolean isDestroyOnShutdown() {
        return destroyOnShutdown;
    }

    /**
     * Enables or disables destroying the process when the JVM shuts down.
     *
     * @param flag {@code true} to destroy on shutdown, {@code false} otherwise
     * @return this builder instance for chaining
     */
    @SuppressWarnings("unused")
    public ManagedProcessBuilder setDestroyOnShutdown(boolean flag) {
        destroyOnShutdown = flag;
        return this;
    }

    /**
     * Sets the number of console lines to retain for diagnostics.
     *
     * @param consoleBufferMaxLines the line count to keep in memory
     * @return this builder instance for chaining
     */
    @SuppressWarnings("unused")
    public ManagedProcessBuilder setConsoleBufferMaxLines(int consoleBufferMaxLines) {
        this.consoleBufferMaxLines = consoleBufferMaxLines;
        return this;
    }

    /**
     * Returns the number of console lines retained for diagnostics.
     *
     * @return the configured line count
     */
    @SuppressWarnings("unused")
    public int getConsoleBufferMaxLines() {
        return consoleBufferMaxLines;
    }

    /**
     * Sets the dispatcher that determines the SLF4J log level (or suppression) for
     * each line emitted on STDOUT/STDERR by the managed process.
     *
     * @param outputStreamLogDispatcher the dispatcher to use
     * @return this builder for chaining
     */
    @SuppressWarnings("unused")
    public ManagedProcessBuilder setOutputStreamLogDispatcher(OutputStreamLogDispatcher outputStreamLogDispatcher) {
        this.outputStreamLogDispatcher = outputStreamLogDispatcher;
        return this;
    }

    /**
     * Returns the dispatcher that determines the SLF4J log level (or suppression) for
     * each STDOUT/STDERR line.
     *
     * @return the current dispatcher
     */
    @SuppressWarnings("unused")
    public OutputStreamLogDispatcher getOutputStreamLogDispatcher() {
        return outputStreamLogDispatcher;
    }

    /**
     * Builds a {@link ManagedProcess} from the current configuration.
     *
     * @return a new {@link ManagedProcess} instance
     */
    public ManagedProcess build() {
        return new ManagedProcess(
                getCommandLine(),
                directory,
                environment,
                inputStream,
                destroyOnShutdown,
                consoleBufferMaxLines,
                outputStreamLogDispatcher,
                stdOuts,
                stdErrs,
                listener,
                isSuccessExitValueChecker);
    }

    /**
     * Provides an input stream which will be fed to the process' STDIN.
     *
     * @param inputStream the input stream to use, or {@code null} to disable
     * @return this builder instance for chaining
     */
    @SuppressWarnings("unused")
    public ManagedProcessBuilder setInputStream(@Nullable InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    /**
     * Adds an extra stream to receive STDOUT from the process.
     *
     * @param stdOutput destination stream for STDOUT
     * @return this builder instance for chaining
     */
    @SuppressWarnings("unused")
    public ManagedProcessBuilder addStdOut(OutputStream stdOutput) {
        stdOuts.add(stdOutput);
        return this;
    }

    /**
     * Adds an extra stream to receive STDERR from the process.
     *
     * @param stdError destination stream for STDERR
     * @return this builder instance for chaining
     */
    @SuppressWarnings("unused")
    public ManagedProcessBuilder addStdErr(OutputStream stdError) {
        stdErrs.add(stdError);
        return this;
    }

    /**
     * Sets the predicate that determines whether an exit value is considered success.
     *
     * @param function predicate receiving the exit value
     * @return this builder instance for chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    public ManagedProcessBuilder setIsSuccessExitValueChecker(IntPredicate function) {
        this.isSuccessExitValueChecker = function;
        return this;
    }

    CommandLine getCommandLine() {
        if (getWorkingDirectory() == null && commonsExecCommandLine.isFile()) {
            Path exec = Path.of(commonsExecCommandLine.getExecutable());
            Path dir = exec.getParent();
            if (dir == null) {
                throw new IllegalStateException(
                        "directory MUST be set (and could not be auto-determined from executable, although it was a File)");
            }
            setWorkingDirectory(dir);
        }
        return commonsExecCommandLine;
    }

    /** Intended for debugging / logging, only. */
    @Override
    public String toString() {
        return commonsExecCommandLine.toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
