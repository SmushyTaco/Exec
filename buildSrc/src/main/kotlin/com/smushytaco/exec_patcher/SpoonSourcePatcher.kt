package com.smushytaco.exec_patcher

import spoon.Launcher
import spoon.compiler.Environment
import spoon.reflect.code.CtBodyHolder
import spoon.reflect.code.CtComment
import spoon.reflect.code.CtStatementList
import spoon.reflect.declaration.*
import spoon.reflect.reference.CtTypeReference
import kotlin.io.path.Path

object SpoonSourcePatcher {
    private const val EXECUTE_WATCHDOG = "org.apache.commons.exec.ExecuteWatchdog"
    private const val DEFAULT_EXECUTOR = "org.apache.commons.exec.DefaultExecutor"

    private const val DURATION = "java.time.Duration"
    private const val THREAD_FACTORY = "java.util.concurrent.ThreadFactory"
    private const val PATH = "java.nio.file.Path"
    private const val EXECUTE_STREAM_HANDLER = "org.apache.commons.exec.ExecuteStreamHandler"

    private const val TIMEOUT_VARIABLE = "timeout"
    private const val WORKING_DIRECTORY_VARIABLE = "workingDirectory"
    private const val THREAD_FACTORY_VARIABLE = "threadFactory"
    private const val EXECUTE_STREAM_HANDLER_VARIABLE = "executeStreamHandler"
    fun main(args: Array<String>) {
        require(args.size == 1) { "Usage: SpoonSourcePatcher <sources-root>" }
        val sourcesRoot = Path(args[0])

        val launcher = Launcher()
        launcher.addInputResource(sourcesRoot.toString())
        launcher.setSourceOutputDirectory(sourcesRoot.toString())

        launcher.environment.noClasspath = true
        launcher.environment.complianceLevel = 21
        launcher.environment.isAutoImports = true
        launcher.environment.prettyPrintingMode = Environment.PRETTY_PRINTING_MODE.AUTOIMPORT

        launcher.buildModel()

        patchExecuteWatchdog(launcher)
        patchDefaultExecutor(launcher)

        launcher.prettyprint()
    }
    private fun Launcher.type(qualifiedName: String) =
        factory.Type().createReference<Any>(qualifiedName)
    private fun CtClass<Any>.hasCtor(parameterTypes: List<CtTypeReference<Any>>): Boolean {
        val want = parameterTypes.map { it.qualifiedName }
        return constructors.any { constructor ->
            constructor.parameters.map { it.type.qualifiedName } == want
        }
    }
    private fun Launcher.makeCtor(parameters: List<Pair<String, String>>, bodyCall: String, parameterDocumentation: Map<String, String>): CtConstructor<Any> {
        val ctor = factory.createConstructor<Any>()
        ctor.addModifier<CtModifiable>(ModifierKind.PROTECTED)
        val javaDoc = StringBuilder()
            .append("Auto-generated constructor by SpoonSourcePatcher.\n")
            .append("This constructor was added by the patch process to mirror the Byte Buddy transformation on the binary.\n")
        for ((_, name) in parameters) {
            javaDoc
                .append("@param ")
                .append(name)
                .append(" ")
                .append(parameterDocumentation.getOrDefault(name, ""))
                .append("\n")
        }
        ctor.addComment<CtElement>(
            factory.createComment(javaDoc.toString(), CtComment.CommentType.JAVADOC)
        )
        for ((type, name) in parameters) {
            factory.createParameter(ctor, type(type), name)
                .addModifier<CtModifiable>(ModifierKind.FINAL)
        }
        val body = factory.createBlock<Any>()
        val call = factory.Code().createCodeSnippetStatement(bodyCall)
        body.addStatement<CtStatementList>(call)
        ctor.setBody<CtBodyHolder>(body)
        return ctor
    }
    private fun CtClass<Any>.addAsLastMember(ctor: CtConstructor<Any>) {
        addTypeMember<CtType<Any>>(ctor)
        val members = typeMembers
        members.remove(ctor)
        members.add(ctor)
        @Suppress("UsePropertyAccessSyntax")
        setTypeMembers<CtType<Any>>(members)
    }
    private fun patchExecuteWatchdog(launcher: Launcher) {
        val theClass = launcher.factory.Class().get<Any>(EXECUTE_WATCHDOG)
            ?: throw IllegalStateException("Class not found: $EXECUTE_WATCHDOG")
        val duration = launcher.type( DURATION)
        val threadFactory = launcher.type( THREAD_FACTORY)
        val signature = listOf(duration, threadFactory)
        if (theClass.hasCtor(signature)) return
        val ctor = launcher.makeCtor(
            listOf(
                Pair(DURATION, TIMEOUT_VARIABLE),
                Pair(THREAD_FACTORY, THREAD_FACTORY_VARIABLE)
            ),
            "this($THREAD_FACTORY_VARIABLE, $TIMEOUT_VARIABLE)",
            mapOf(
                TIMEOUT_VARIABLE to "The timeout Duration for the process. It must be greater than 0 or {@code INFINITE_TIMEOUT_DURATION}.",
                THREAD_FACTORY_VARIABLE to "The thread factory."
            )
        )
        theClass.addAsLastMember(ctor)
    }
    private fun patchDefaultExecutor(launcher: Launcher) {
        val theClass = launcher.factory.Class().get<Any>(DEFAULT_EXECUTOR)
            ?: throw IllegalStateException("Class not found: $DEFAULT_EXECUTOR")
        val path = launcher.type( PATH)
        val threadFactory = launcher.type( THREAD_FACTORY)
        val executeStreamHandler = launcher.type( EXECUTE_STREAM_HANDLER)
        val signature = listOf(path, threadFactory, executeStreamHandler)
        if (theClass.hasCtor(signature)) return
        val ctor = launcher.makeCtor(
            listOf(
                Pair(PATH, WORKING_DIRECTORY_VARIABLE),
                Pair(THREAD_FACTORY, THREAD_FACTORY_VARIABLE),
                Pair(EXECUTE_STREAM_HANDLER, EXECUTE_STREAM_HANDLER_VARIABLE)
            ),
            "this($THREAD_FACTORY_VARIABLE, $EXECUTE_STREAM_HANDLER_VARIABLE, $WORKING_DIRECTORY_VARIABLE)",
            mapOf(
                WORKING_DIRECTORY_VARIABLE to "The working directory of the process.",
                THREAD_FACTORY_VARIABLE to "The thread factory.",
                EXECUTE_STREAM_HANDLER_VARIABLE to "Taking care of output and error stream."
            )
        )
        theClass.addAsLastMember(ctor)
    }
}