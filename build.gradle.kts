plugins { `java-library` }
val name = providers.gradleProperty("name")
val projectGroup = providers.gradleProperty("group")
val projectVersion = providers.gradleProperty("version")
val commonsExecVersion = providers.gradleProperty("commons_exec_version")
val commonsIoVersion = providers.gradleProperty("commons_io_version")
val jspecifyVersion = providers.gradleProperty("jspecify_version")
val slf4jVersion = providers.gradleProperty("slf4j_version")
val commonsLang3Version = providers.gradleProperty("commons_lang3_version")
val junitJupiterVersion = providers.gradleProperty("junit_jupiter_version")
val assertjCoreVersion = providers.gradleProperty("assertj_core_version")
val javaVersion = providers.gradleProperty("java_version")
description = "Java library to launch external processes"
base.archivesName = name.get()
group = projectGroup.get()
version = projectVersion.get()
repositories { mavenCentral() }
dependencies {
    implementation("org.apache.commons:commons-exec:${commonsExecVersion.get()}")
    implementation("commons-io:commons-io:${commonsIoVersion.get()}")
    implementation("org.jspecify:jspecify:${jspecifyVersion.get()}")
    implementation("org.slf4j:slf4j-api:${slf4jVersion.get()}")

    testImplementation("org.apache.commons:commons-lang3:${commonsLang3Version.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion.get()}")
    testImplementation("org.assertj:assertj-core:${assertjCoreVersion.get()}")
    testRuntimeOnly("org.slf4j:slf4j-simple:${slf4jVersion.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${junitJupiterVersion.get()}")
}
configurations
    .matching { it.isCanBeResolved }
    .configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.apache.commons:commons-exec"))
                .using(project(":commons-exec-patched"))
                .because("Use locally patched commons-exec produced by :commons-exec-patched")
        }
    }

tasks {
    java {
        toolchain.languageVersion = JavaLanguageVersion.of(javaVersion.get())
        sourceCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        targetCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
    }
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
}