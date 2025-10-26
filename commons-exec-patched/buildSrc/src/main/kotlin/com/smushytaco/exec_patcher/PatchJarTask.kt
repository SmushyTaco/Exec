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
