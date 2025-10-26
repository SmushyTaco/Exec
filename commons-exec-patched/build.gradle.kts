import com.smushytaco.exec_patcher.PatchJarTask
import com.smushytaco.exec_patcher.PatchSourcesJarTask
plugins {
    base
    `maven-publish`
}
val commonsExecVersion = providers.gradleProperty("commons_exec_version")
val javaVersion = providers.gradleProperty("java_version")
repositories { mavenCentral() }
val commonsExecToPatch by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}
val commonsExecSourcesToPatch by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}
dependencies {
    commonsExecToPatch("org.apache.commons:commons-exec:${commonsExecVersion.get()}")
    commonsExecSourcesToPatch("org.apache.commons:commons-exec:${commonsExecVersion.get()}:sources@jar")
}
val patchCommonsExec by tasks.registering(PatchJarTask::class) {
    inputJar.set(layout.file(provider { commonsExecToPatch.singleFile }))
    outputJar.set(layout.buildDirectory.file("libs/commons-exec-${commonsExecVersion.get()}-patched.jar"))
}
val patchCommonsExecSources by tasks.registering(PatchSourcesJarTask::class) {
    inputSourcesJar.set(layout.file(provider { commonsExecSourcesToPatch.singleFile }))
    expandedDir.set(layout.buildDirectory.dir("tmp/patched-sources"))
    outputSourcesJar.set(layout.buildDirectory.file("libs/commons-exec-${commonsExecVersion.get()}-patched-sources.jar"))
}

artifacts.add("default", patchCommonsExec.flatMap { it.outputJar }) {
    builtBy(patchCommonsExec)
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.get()
        targetCompatibility = javaVersion.get()
        options.release = javaVersion.get().toInt()
    }
    withType<JavaExec>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<Javadoc>().configureEach { options.encoding = "UTF-8" }
    withType<Test>().configureEach {
        defaultCharacterEncoding = "UTF-8"
        useJUnitPlatform()
    }
    named("assemble").configure { dependsOn(patchCommonsExec, patchCommonsExecSources) }
}
publishing {
    publications {
        create<MavenPublication>("patched") {
            groupId = "org.apache.commons"
            artifactId = "commons-exec"
            version = "${commonsExecVersion.get()}-patched"

            artifact(patchCommonsExec.flatMap { it.outputJar }) {
                builtBy(patchCommonsExec)
            }
            artifact(patchCommonsExecSources.flatMap { it.outputSourcesJar }) {
                builtBy(patchCommonsExecSources)
                classifier = "sources"
            }
        }
    }
    repositories {
        maven {
            name = "localPatched"
            url = uri(layout.buildDirectory.dir("local-m2"))
        }
    }
}