val name = providers.gradleProperty("name")
rootProject.name = name.get()
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    val yumiGradleLicenserVersion = providers.gradleProperty("yumi_gradle_licenser_version")
    plugins { id("dev.yumi.gradle.licenser").version(yumiGradleLicenserVersion.get()) }
}