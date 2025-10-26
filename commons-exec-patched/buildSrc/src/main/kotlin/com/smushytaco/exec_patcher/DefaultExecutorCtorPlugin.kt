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
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.pool.TypePool
import java.nio.file.Path
import java.util.concurrent.ThreadFactory

class DefaultExecutorCtorPlugin: Plugin {
    override fun apply(
        builder: DynamicType.Builder<*>,
        typeDescription: TypeDescription,
        classFileLocator: ClassFileLocator
    ): DynamicType.Builder<*> {
        val pool = TypePool.Default.of(classFileLocator)
        val threadFactory = TypeDescription.ForLoadedType.of(ThreadFactory::class.java)
        val execStreamHandler = pool.describe("org.apache.commons.exec.ExecuteStreamHandler").resolve()
        val path = TypeDescription.ForLoadedType.of(Path::class.java)
        val superCtor =
            typeDescription.declaredMethods.filter(
                    ElementMatchers.isConstructor<MethodDescription.InDefinedShape>()
                    .and(ElementMatchers.takesArguments(threadFactory, execStreamHandler, path)))
                .only
        return builder
            .defineConstructor(Visibility.PROTECTED)
            .withParameter(path, "workingDirectory")
            .withParameter(threadFactory, "threadFactory")
            .withParameter(execStreamHandler, "executeStreamHandler")
            .intercept(
                MethodCall.invoke(superCtor)
                .withArgument(1)  // ThreadFactory
                .withArgument(2)  // ExecuteStreamHandler
                .withArgument(0)  // Path
            )
    }
    override fun matches(target: TypeDescription?) =
        target != null && target.name == "org.apache.commons.exec.DefaultExecutor"
    override fun close() {}
}