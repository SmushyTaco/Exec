plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.gradleup.shadow")
    id("org.jetbrains.dokka")
    id("dev.yumi.gradle.licenser")
    id("co.uzzu.dotenv.gradle")
    id("com.gradleup.nmcp")
}
val projectName = providers.gradleProperty("name")
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
val dokkaVersion = providers.gradleProperty("dokka_version")
val projectDescription = "Java library used to launch external processes."
description = projectDescription
base.archivesName = projectName.get()
group = projectGroup.get()
version = projectVersion.get()
repositories {
    mavenCentral()
    maven {
        name = "localPatched"
        url = uri("$rootDir/commons-exec-patched/build/local-m2")
    }
}
dependencies {
    dokkaPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:${dokkaVersion.get()}")
    shadow("commons-io:commons-io:${commonsIoVersion.get()}")
    shadow("org.jspecify:jspecify:${jspecifyVersion.get()}")
    shadow("org.slf4j:slf4j-api:${slf4jVersion.get()}")
    implementation("org.apache.commons:commons-exec:${commonsExecVersion.get()}-patched")

    testImplementation("org.apache.commons:commons-lang3:${commonsLang3Version.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion.get()}")
    testImplementation("org.assertj:assertj-core:${assertjCoreVersion.get()}")
    testRuntimeOnly("org.slf4j:slf4j-simple:${slf4jVersion.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${junitJupiterVersion.get()}")
}
tasks {
    java {
        toolchain.languageVersion = JavaLanguageVersion.of(javaVersion.get())
        sourceCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        targetCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        withSourcesJar()
        withJavadocJar()

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
    shadowJar {
        archiveClassifier = ""
        dependencies {
            include(dependency("org.apache.commons:commons-exec"))
        }
        relocate("org.apache.commons.exec", "${project.group.toString().lowercase()}.${base.archivesName.get().lowercase().replace('-', '_')}.shaded.org.apache.commons.exec")
    }
    named("build") {
        dependsOn(shadowJar, named("dokkaJar"))
    }
    register<Jar>("dokkaJar") {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        dependsOn(dokkaGenerateHtml)
        archiveClassifier = "dokka"
        from(layout.buildDirectory.dir("dokka/html"))
    }
}
license {
    rule(file("./HEADER"))
    include("**/*.java")
    include("**/*.kt")
    exclude("**/*.properties")
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["shadow"])
            groupId = project.group.toString()
            artifactId = base.archivesName.get()
            version = project.version.toString()
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
            artifact(tasks.named("dokkaJar"))
            pom {
                name = projectName
                description = projectDescription
                url = "https://github.com/SmushyTaco/Exec"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "smushytaco"
                        name = "Nikan Radan"
                        email = "personal@nikanradan.com"
                    }
                }
                scm {
                    url = "https://github.com/SmushyTaco/Exec"
                    connection = "scm:git:https://github.com/SmushyTaco/Exec.git"
                    developerConnection = "scm:git:ssh://git@github.com/SmushyTaco/Exec.git"
                }
            }
        }
    }
}
signing {
    val keyFile = layout.projectDirectory.file("./private-key.asc")
    if (keyFile.asFile.exists()) {
        isRequired = true
        useInMemoryPgpKeys(
            providers.fileContents(keyFile).asText.get(),
            env.fetch("PASSPHRASE", "")
        )
        sign(publishing.publications)
    }
}
nmcp {
    publishAllPublicationsToCentralPortal {
        username = env.fetch("USERNAME_TOKEN", "")
        password = env.fetch("PASSWORD_TOKEN", "")
        publishingType = "USER_MANAGED"
    }
}