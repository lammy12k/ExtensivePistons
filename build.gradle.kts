import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("26.3.build.+")

    compileOnly("org.spongepowered:mixin:${providers.gradleProperty("mixin_version").get()}")
    compileOnly("io.github.llamalad7:mixinextras-common:${providers.gradleProperty("mixinextras_version").get()}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${providers.gradleProperty("mixinextras_version").get()}")

    implementation("org.incendo:cloud-paper:${providers.gradleProperty("cloud-paper_version").get()}")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    val copyPluginToRun = register<Copy>("copyPluginToRun") {
        description = ""
        dependsOn(shadowJar)
        from(shadowJar.flatMap { it.archiveFile })
        into(layout.projectDirectory.dir("run/plugins"))
    }

    runServer {
        dependsOn("copyPluginToRun")
        minecraftVersion("26.3")
        jvmArgs(
            "-Xms2G",
            "-Xmx2G",
            "-javaagent:plugins/MixinLoader-2.1.0.jar",
            "-Dcom.mojang.eula.agree=true"
        )
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

val hangarTokenProvider = providers.fileContents(layout.projectDirectory.file("importantconf.conf"))
    .asText
    .map { content ->
        content.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("hangar-key") && !it.startsWith("#") }
            ?.substringAfter("=")
            ?.trim()
            ?.removeSurrounding("\"")
            ?.removeSurrounding("'")
            ?: error("hangar-key not found in importantconf.conf")
    }

hangarPublish {
    publications.register("plugin") {
        version.set(project.version.toString())
        channel.set("Release")
        id.set("extensivepistons")
        apiKey.set(hangarTokenProvider)
        changelog.set(
            providers.fileContents(layout.projectDirectory.file("CHANGELOG.md"))
                .asText
                .orElse("")
        )

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(listOf("26.3"))
            }
        }
    }
}