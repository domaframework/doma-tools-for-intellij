import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.gradle.internal.classpath.Instrumented.systemProperty
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import java.net.URL
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
    implementation(libs.jackson)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.domacore)

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        pluginVerifier(version = "1.383")
        zipSigner()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
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

tasks.register("updateChangelog") {
    group = "changelog"
    description = "Update CHANGELOG.md based on merged PRs since last release"

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Label(
        val id: Long = 0,
        val name: String = "",
        val color: String = "",
        val description: String? = "",
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PullRequestItem(
        val title: String = "",
        @JsonProperty("html_url")
        val url: String = "",
        val number: Long = 0,
        var labelItems: List<String> = emptyList(),
    )

    class VersionInfo(
        lastversion: String = "",
    ) {
        var lastMajor: Int = 0
        var lastMinor: Int = 0
        var lastPatch: Int = 0

        var major: Int = 0
        var minor: Int = 0
        var patch: Int = 0

        init {
            val lastVersions = lastversion.substringAfter("v").split(".")
            lastMajor = lastVersions[0].toInt()
            lastMinor = lastVersions[1].toInt()
            lastPatch = lastVersions[2].toInt()

            major = lastMajor
            minor = lastMinor
            patch = lastPatch
        }

        fun updateMajor() {
            if (major == lastMajor) {
                major++
                minor = 0
                patch = 0
            }
        }

        fun updateMinor() {
            if (minor == lastMinor && major == lastMajor) {
                minor++
                patch = 0
            }
        }

        fun updatePatch() {
            if (minor == lastMinor && major == lastMajor && patch == lastPatch) {
                patch++
            }
        }

        fun getNewVersion() = "$major.$minor.$patch"
    }

    val releaseDate =
        if (project.hasProperty("releaseDate")) {
            project.property("releaseDate") as String
        } else {
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
    val changelogFile = project.file("CHANGELOG.md")
    val rootDir = project.rootDir

    @Suppress("UNCHECKED_CAST")
    doLast {
        fun runCommand(command: String): String {
            val parts = command.split(" ")
            return ProcessBuilder(parts)
                .directory(rootDir)
                .redirectErrorStream(true)
                .start()
                .inputStream
                .bufferedReader()
                .readText()
                .trim()
        }

        val tagsOutput = runCommand("git tag --sort=-v:refname")
        val semverRegex = Regex("^\\d+\\.\\d+\\.\\d+$")
        val tags = tagsOutput.lines().filter { semverRegex.matches(it) }
        if (tags.isEmpty()) {
            throw GradleException("Not Found Release Tag")
        }
        val lastTag = tags.first()
        println("Last release tag: $lastTag")

        val lastReleaseCommitDate = runCommand("git log -1 --format=%cI $lastTag").trim()
        val offsetTime = OffsetDateTime.parse(lastReleaseCommitDate)
        val lastReleaseCommitDateUtc = offsetTime.withOffsetSameInstant(ZoneOffset.UTC)
        println("Last release commit date: $lastReleaseCommitDateUtc")

        val githubToken = System.getenv("GITHUB_TOKEN") ?: throw GradleException("Not Setting GITHUB_TOKEN")
        val repo = System.getenv("GITHUB_REPOSITORY") ?: throw GradleException("Not Setting GITHUB_REPOSITORY")

        // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests
        val apiPath = "https://api.github.com/search/issues"
        val sort = "sort:updated"
        val status = "is:closed+is:merged"
        val type = "type:pr"
        val base = "+base:main"
        val apiUrl = "$apiPath?q=repo:$repo+$type+$status+$sort+$base+merged:>$lastReleaseCommitDateUtc"
        val connection =
            URL(apiUrl).openConnection().apply {
                setRequestProperty("Authorization", "token $githubToken")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }
        val response = connection.getInputStream().bufferedReader().readText()
        val mapper = jacksonObjectMapper()
        val json: Map<String, Any> =
            (
                mapper.readValue(response, Map::class.java) as? Map<String, Any>
                    ?: emptyList<Map<String, Any>>()
            ) as Map<String, Any>
        val items =
            (json["items"] as List<*>)
                .mapNotNull { item ->
                    mapper.convertValue(item, Map::class.java) as Map<String, Any>
                }

        val prList =
            items.mapNotNull { pr ->
                val labelTemps = pr["labels"] as List<Map<String, Any>>
                val labels =
                    labelTemps
                        .mapNotNull { mapper.convertValue(it, Label::class.java) }
                        .map { it.name }
                mapper.convertValue(pr, PullRequestItem::class.java)?.apply {
                    labelItems = labels
                }
            }

        val categories =
            mapOf(
                "New Features" to listOf("feature", "enhancement"),
                "Bug Fixes" to listOf("fix", "bug", "bugfix"),
                "Maintenance" to listOf("ci", "chore", "perf", "refactor", "test", "security"),
                "Documentation" to listOf("doc"),
                "Dependency Updates" to listOf("dependencies"),
            )

        val versionUpLabels =
            mapOf(
                "major" to listOf("major"),
                "minor" to listOf("minor", "feature", "enhancement"),
                "patch" to listOf("patch"),
            )

        val categorized: MutableMap<String, MutableList<PullRequestItem>> =
            mutableMapOf(
                "New Features" to mutableListOf(),
                "Bug Fixes" to mutableListOf(),
                "Maintenance" to mutableListOf(),
                "Documentation" to mutableListOf(),
                "Dependency Updates" to mutableListOf(),
                "Other" to mutableListOf(),
            )

        val versionInfo = VersionInfo(lastTag)
        var assigned: Boolean

        prList.forEach { pr ->
            assigned = false
            categories.forEach { (category, catLabels) ->
                val prLabels = pr.labelItems
                if (prLabels.any { it in catLabels }) {
                    categorized[category]?.add(pr)
                    versionUpLabels.forEach { (version, versionUpLabels) ->
                        if (prLabels.any { it in versionUpLabels }) {
                            assigned = true
                            when (version) {
                                "major" -> versionInfo.updateMajor()

                                "minor" -> versionInfo.updateMinor()

                                "patch" -> versionInfo.updatePatch()
                            }
                        }
                    }
                }
                if (!assigned) {
                    versionInfo.updatePatch()
                    categorized["Other"]?.add(pr)
                }
            }
        }

        val newVersion = versionInfo.getNewVersion()
        val prLinks = mutableListOf<String>()
        val newEntry = StringBuilder()

        newEntry.append("## [$newVersion] - $releaseDate\n\n")
        categories.keys.forEach { category ->
            val hitItems = categorized[category]
            if (!hitItems.isNullOrEmpty()) {
                newEntry.append("### $category\n\n")
                hitItems.forEach { title ->
                    newEntry.append("- ${title.title} ([#${title.number}])\n")
                    prLinks.add("[#${title.number}]:${title.url}")
                }
                newEntry.append("\n")
            }
        }

        prLinks.forEach { link -> newEntry.append("$link\n") }

        val currentContent = if (changelogFile.exists()) changelogFile.readText() else "\n"
        val updatedContent =
            if (currentContent.contains("## [Unreleased]")) {
                currentContent.replace("## [Unreleased]", "## [Unreleased]\n\n$newEntry")
            } else {
                "## [Unreleased]\n\n$newEntry$currentContent"
            }
        val repoUrl = "https://github.com/domaframework/doma-tools-for-intellij"
        changelogFile.writeText(updatedContent)
        changelogFile.appendText("[$newVersion]: $repoUrl/compare/$lastTag...$newVersion\n")

        val githubEnv = System.getenv("GITHUB_ENV")
        val envFile = File(githubEnv)
        envFile.appendText("NEW_VERSION=$newVersion\n")
        envFile.appendText("BRANCH=doc/changelog-update-$newVersion\n")

        println("Update CHANGELOG.md :newVersion $newVersion")
    }
}

tasks.register("checkExistChangelogPullRequest") {
    group = "changelog"
    description = "Check if a PR with the same name has already been created"

    val newBranch =
        if (project.hasProperty("newBranch")) {
            project.property("newBranch") as String
        } else {
            "doc/changelog-update"
        }

    @Suppress("UNCHECKED_CAST")
    doLast {
        println("Check PR with the same name has already been created $newBranch")

        val githubToken = System.getenv("GITHUB_TOKEN") ?: throw GradleException("Not Setting GITHUB_TOKEN")
        val repo = System.getenv("GITHUB_REPOSITORY") ?: throw GradleException("Not Setting GITHUB_REPOSITORY")

        // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests
        val apiPath = "https://api.github.com/search/issues"
        val status = "is:open"
        val type = "type:pr"
        val label = "label:changelog,skip-changelog"
        val branch = "base:main+head:$newBranch"
        val apiUrl = "$apiPath?q=repo:$repo+$branch+$label+$status+$type"
        val connection =
            URL(apiUrl).openConnection().apply {
                setRequestProperty("Authorization", "token $githubToken")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }
        val response = connection.getInputStream().bufferedReader().readText()
        val mapper = jacksonObjectMapper()
        val json: Map<String, Any> =
            (
                mapper.readValue(response, Map::class.java) as? Map<String, Any>
                    ?: emptyList<Map<String, Any>>()
            ) as Map<String, Any>
        println("get response Json ${json["total_count"]}")
        val existChangelogPr = json["total_count"] != 0

        val githubEnv = System.getenv("GITHUB_ENV")
        File(githubEnv).appendText("EXIST_CHANGELOG=$existChangelogPr\n")
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

val projectEncoding: String by project

fun replaceVersionInPluginUtil(
    ver: String,
    encoding: String,
) {
    ant.withGroovyBuilder {
        "replaceregexp"(
            "match" to """(const val PLUGIN_VERSION = ")(\d+\.\d+\.\d+)((?:-beta)*)""",
            "replace" to "\\1$ver",
            "encoding" to encoding,
            "flags" to "g",
        ) {
            "fileset"("dir" to ".") {
                "include"("name" to "**/PluginUtil.kt")
            }
        }
    }
}

fun replaceVersionGradleProperty(
    ver: String,
    encoding: String,
) {
    ant.withGroovyBuilder {
        "replaceregexp"(
            "match" to """(pluginVersion = )(\d+\.\d+\.\d+)((?:-beta)*)""",
            "replace" to "\\1$ver",
            "encoding" to encoding,
            "flags" to "g",
        ) {
            "fileset"("dir" to ".") {
                "include"("name" to "**/gradle.properties")
            }
        }
    }
}

fun replaceVersionInLogSetting(
    ver: String,
    encoding: String,
) {
    ant.withGroovyBuilder {
        "replaceregexp"(
            "match" to """(org.domaframework.doma.intellij.plugin.version:-)(\d+\.\d+\.\d+)((?:-beta)*)(})""",
            "replace" to "\\1$ver\\4",
            "encoding" to encoding,
            "flags" to "g",
        ) {
            "fileset"("dir" to ".") {
                "include"("name" to "**/logback.xml")
                "include"("name" to "**/logback-test.xml")
            }
        }
    }
}

fun replaceVersion(
    ver: String,
    encoding: String,
) {
    checkNotNull(ver)
    replaceVersionInPluginUtil(ver, encoding)
    replaceVersionGradleProperty("$ver-beta", encoding)
    replaceVersionInLogSetting(ver, encoding)
    println("Replace version in PluginUtil.kt, gradle.properties, logback.xml")

    val githubEnv = System.getenv("GITHUB_ENV")
    val envFile = File(githubEnv)
    envFile.appendText("REPLACE_VERSION=$ver\n")

    println("Set Replace version in GITHUB_ENV: $ver")
}

tasks.register("replaceNewVersion") {
    val releaseVersion =
        if (project.hasProperty("newVersion")) {
            project.property("newVersion") as String
        } else {
            "0.0.0"
        }

    val encoding = projectEncoding
    doLast {
        val lastVersions = releaseVersion.substringAfter("v").split(".")
        val major = lastVersions[0].toInt()
        val minor = lastVersions[1].toInt()
        val patch = lastVersions[2].toInt() + 1

        val newVersion = "$major.$minor.$patch"
        println("Release newVersion: $newVersion")
        replaceVersion(newVersion, encoding)
    }
}

tasks.register("replaceDraftVersion") {
    val draftVersion =
        if (project.hasProperty("draftVersion")) {
            project.property("draftVersion") as String
        } else {
            "0.0.0"
        }
    val encoding = projectEncoding

    doLast {
        println("Release DraftVersion: $draftVersion")
        replaceVersion(draftVersion, encoding)
    }
}
