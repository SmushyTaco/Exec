val name = providers.gradleProperty("name")
rootProject.name = name.get()
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    val shadowVersion = providers.gradleProperty("shadow_version")
    val dokkaVersion = providers.gradleProperty("dokka_version")
    val yumiGradleLicenserVersion = providers.gradleProperty("yumi_gradle_licenser_version")
    plugins {
        id("com.gradleup.shadow").version(shadowVersion.get())
        id("org.jetbrains.dokka").version(dokkaVersion.get())
        id("dev.yumi.gradle.licenser").version(yumiGradleLicenserVersion.get())
    }
}
if (!file("./commons-exec-patched/build/local-m2").exists()) {
    providers.exec {
        val gradlew = if (System.getProperty("os.name").lowercase().contains("win"))
            file("${rootDir}/gradlew.bat").absolutePath
        else
            file("${rootDir}/gradlew").absolutePath
        commandLine(gradlew, "publishPatchedPublicationToLocalPatchedRepository")
        workingDir(file("$rootDir/commons-exec-patched"))
    }.result.get().also { it.rethrowFailure().assertNormalExitValue() }
}