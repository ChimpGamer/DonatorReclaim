plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    implementation("dev.dejvokep:boosted-yaml:1.3.1")
}

group = "nl.chimpgamer"
version = "2.0.0-SNAPSHOT"
description = "DonatorReclaim"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        expand("version" to project.version)
    }

    shadowJar {
        minimize()

        archiveFileName.set("${project.name}-v${project.version}.jar")
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}
