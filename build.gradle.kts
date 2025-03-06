import org.gradle.internal.classpath.Instrumented.systemProperty
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import java.util.Base64

plugins {
    id("java")
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
    alias(libs.plugins.grammarkit)
}

grammarKit {
    tasks {
        generateLexer {
            group = "grammer-kit"
            sourceFile = file("src/main/java/org/domaframework/doma/intellij/Sql.flex")
            targetOutputDir = file("src/main/gen/org/domaframework/doma/intellij")
            purgeOldFiles = true
        }
        generateParser {
            group = "grammer-kit"
            sourceFile = file("src/main/java/org/domaframework/doma/intellij/Sql.bnf")
            targetRootOutputDir = file("src/main/gen")
            pathToParser = "/org/domaframework/doma/intellij/SqlParser.java"
            pathToPsiRoot = "/org/domaframework/doma/intellij/psi"
            purgeOldFiles = true
        }
    }
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(17)
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

sourceSets {
    main {
        java {
            sourceSets["main"].java.srcDirs("src/main/gen")
            sourceSets["main"].kotlin.srcDirs("src/main/kotlin")
        }
    }

    test {
        systemProperty("user.language", "ja")
    }
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.logback)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.domacore)

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
        testFramework(TestFrameworkType.Metrics)
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        description =
            providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                with(it.lines()) {
                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
                }
            }

        val changelog = project.changelog
        changeNotes =
            providers.gradleProperty("pluginVersion").map { pluginVersion ->
                with(changelog) {
                    renderItem(
                        (getOrNull(pluginVersion) ?: getUnreleased())
                            .withHeader(false)
                            .withEmptySections(false),
                        Changelog.OutputType.HTML,
                    )
                }
            }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels =
            providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }
}

tasks.register("encodeBase64") {
    doLast {
        val currentDir = File("./certificate")
        val files = currentDir.listFiles() ?: return@doLast

        for (file in files) {
            val fileName = file.name

            if (fileName == "build.gradle.kts" || fileName.endsWith(".example")) continue

            val fileData = file.readBytes()
            val encodedData = Base64.getEncoder().encodeToString(fileData)

            println("File Name: $fileName")
            println("Encoded Data:")
            println(encodedData)
            println("\n\n\n")
        }
    }
}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders +=
                    CommandLineArgumentProvider {
                        listOf(
                            "-Drobot-server.port=8082",
                            "-Dide.mac.message.dialogs.as.sheets=false",
                            "-Djb.privacy.policy.text=<!--999.999-->",
                            "-Djb.consents.confirmation.enabled=false",
                            "-Didea.log.registry.conflicts.silent=true",
                        )
                    }
            }

            plugins {
                robotServerPlugin()
            }
        }
    }
}

spotless {
    val targetExclude =
        listOf(
            "src/test/kotlin/org/domaframework/doma/intellij/*.kt",
            "src/test/testData/**",
            "src/main/gen/**",
        )
    val licenseHeaderFile = rootProject.file("spotless/copyright.java")

    lineEndings = com.diffplug.spotless.LineEnding.UNIX
    java {
        targetExclude(targetExclude)
        googleJavaFormat(
            libs.google.java.format
                .get()
                .version,
        )
        licenseHeaderFile(licenseHeaderFile)
    }
    // https://github.com/diffplug/spotless/issues/532
    format("javaMisc") {
        targetExclude(targetExclude)
        target("src/*.java")
        licenseHeaderFile(licenseHeaderFile, "(package|module|\\/\\*\\*)")
    }
    kotlin {
        ktlint(libs.ktlint.get().version)
        licenseHeaderFile(licenseHeaderFile)
    }
    kotlinGradle {
        ktlint(libs.ktlint.get().version)
    }
    format("misc") {
        target("**/*.gitignore", "docs/**/*.rst", "**/*.md")
        targetExclude("**/bin/**", "**/build/**")
        leadingTabsToSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
