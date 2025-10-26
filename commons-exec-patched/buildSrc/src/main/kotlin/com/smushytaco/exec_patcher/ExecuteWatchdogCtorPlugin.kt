package com.smushytaco.exec_patcher

import net.bytebuddy.build.Plugin
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.matcher.ElementMatchers
import java.time.Duration
import java.util.concurrent.ThreadFactory

class ExecuteWatchdogCtorPlugin: Plugin {
    override fun apply(
        builder: DynamicType.Builder<*>,
        typeDescription: TypeDescription,
        classFileLocator: ClassFileLocator
    ): DynamicType.Builder<*> {
        val threadFactory = TypeDescription.ForLoadedType.of(ThreadFactory::class.java)
        val duration = TypeDescription.ForLoadedType.of(Duration::class.java)
        val superCtor =
            typeDescription.declaredMethods
                .filter(
                    ElementMatchers.isConstructor<MethodDescription.InDefinedShape>()
                    .and(ElementMatchers.takesArguments(threadFactory, duration)))
                .only
        return builder
            .defineConstructor(Visibility.PROTECTED)
            .withParameter(Duration::class.java, "timeout")
            .withParameter(ThreadFactory::class.java, "threadFactory")
            .intercept(
                MethodCall.invoke(superCtor)
                .withArgument(1)  // ThreadFactory
                .withArgument(0)  // Duration
            )
    }
    override fun matches(target: TypeDescription?) =
        target != null && target.name == "org.apache.commons.exec.ExecuteWatchdog"
    override fun close() {}
}