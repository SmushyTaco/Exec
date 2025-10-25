import com.smushytaco.exec_patcher.PatchJarTask
plugins { base }
repositories { mavenCentral() }
val commonsExecVersion = providers.gradleProperty("commons_exec_version")
val commonsExecToPatch by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}
dependencies { commonsExecToPatch("org.apache.commons:commons-exec:${commonsExecVersion.get()}") }
val patchCommonsExec by tasks.registering(PatchJarTask::class) {
    inputJar.set(layout.file(provider { commonsExecToPatch.singleFile }))
    outputJar.set(layout.buildDirectory.file("libs/commons-exec-${commonsExecVersion.get()}-patched.jar"))
}
artifacts.add("default", patchCommonsExec.flatMap { it.outputJar }) {
    builtBy(patchCommonsExec)
}