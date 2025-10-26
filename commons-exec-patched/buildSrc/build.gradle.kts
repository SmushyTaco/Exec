import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    `kotlin-dsl`
    id("dev.yumi.gradle.licenser")
}
val name = providers.gradleProperty("name")
val projectGroup = providers.gradleProperty("group")
val projectVersion = providers.gradleProperty("version")
val byteBuddyVersion = providers.gradleProperty("byte_buddy_version")
val spoonCoreVersion = providers.gradleProperty("spoon_core_version")
val javaVersion = providers.gradleProperty("java_version")
base.archivesName = name.get()
group = projectGroup.get()
version = projectVersion.get()
repositories { mavenCentral() }
dependencies {
    implementation("net.bytebuddy:byte-buddy:${byteBuddyVersion.get()}")
    implementation("fr.inria.gforge.spoon:spoon-core:${spoonCoreVersion.get()}")
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
    withType<Test>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            extraWarnings = true
            jvmTarget = JvmTarget.valueOf("JVM_${javaVersion.get()}")
        }
    }
    java {
        toolchain.languageVersion = JavaLanguageVersion.of(javaVersion.get())
        sourceCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        targetCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        withSourcesJar()
    }
}
license {
    rule(file("../../HEADER"))
    include("**/*.java")
    include("**/*.kt")
    exclude("**/*.properties")
}