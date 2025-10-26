val name = providers.gradleProperty("name")
rootProject.name = name.get()
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    val shadowVersion = providers.gradleProperty("shadow_version")
    val dokkaVersion = providers.gradleProperty("dokka_version")
    plugins {
        id("com.gradleup.shadow").version(shadowVersion.get())
        id("org.jetbrains.dokka").version(dokkaVersion.get())
    }
}
providers.exec {
    val gradlew = if (System.getProperty("os.name").lowercase().contains("win"))
        file("${rootDir}${File.separatorChar}gradlew.bat").absolutePath
    else
        file("${rootDir}${File.separatorChar}gradlew").absolutePath
    commandLine(gradlew, "publishPatchedPublicationToLocalPatchedRepository")
    workingDir(file("$rootDir${File.separatorChar}commons-exec-patched"))
}.result.get().also { it.rethrowFailure().assertNormalExitValue() }