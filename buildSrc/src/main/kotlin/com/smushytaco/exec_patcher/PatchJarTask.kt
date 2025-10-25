package com.smushytaco.exec_patcher

import net.bytebuddy.build.Plugin
import net.bytebuddy.build.Plugin.Engine
import net.bytebuddy.build.Plugin.Engine.Default
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.nio.file.Files

@CacheableTask
abstract class PatchJarTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val inputJar: RegularFileProperty

    @get:OutputFile
    abstract val outputJar: RegularFileProperty

    @TaskAction
    fun run() {
        val inFile = inputJar.get().asFile
        val outFile = outputJar.get().asFile
        Files.createDirectories(outFile.toPath().parent)
        val engine: Engine = Default()
        engine.apply(
            Engine.Source.ForJarFile(inFile),
            Engine.Target.ForJarFile(outFile),
            listOf(
                Plugin.Factory.UsingReflection(DefaultExecutorCtorPlugin::class.java),
                Plugin.Factory.UsingReflection(ExecuteWatchdogCtorPlugin::class.java)
            )
        )
    }
}
