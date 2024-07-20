import org.asciidoctor.gradle.editorconfig.AsciidoctorEditorConfigGenerator
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.jvm.pdf.AsciidoctorPdfTask
import java.text.SimpleDateFormat
import java.util.Date

buildscript {
    configurations.classpath {
        resolutionStrategy.eachDependency {
            val requested = requested
            if (requested.group == "com.burgstaller" && requested.name == "okhttp-digest" && requested.version == "1.10") {
                useTarget("io.github.rburgst:${requested.name}:1.21")
                because("Dependency has moved")
            }
        }
    }
}

plugins {
    kotlin("jvm") version "2.0.0"

    id("org.asciidoctor.jvm.convert") version "4.0.2"
    id("org.asciidoctor.jvm.pdf") version "4.0.2"
    id("org.asciidoctor.jvm.gems") version "4.0.2"
    id("org.asciidoctor.editorconfig") version "4.0.2"
}

group = "net.coderdojo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    ruby.gems()
}

dependencies {
    add("asciidoctorGems", "rubygems:rouge:4.2.0")
}


sourceSets {
    create("docs") {
        kotlin {
            compileClasspath += main.get().output
            runtimeClasspath += output + compileClasspath
        }
    }
    val docs by getting {
        dependencies {
            "docsImplementation"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1")
            "docsImplementation"("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
        }
    }
}


tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "21"
    }
}

// Asciidoctor

val asciiAttributes = mapOf(
    "imagesdir" to ".",
    "toc" to "left",
    "toclevels" to 3,
    "max-width" to "100%",
    "projectName" to rootProject.name,
    "dateTime" to SimpleDateFormat("dd-MM-yyyy HH:mm:ssZ").format(Date())
)

tasks.withType(AsciidoctorTask::class) {
    setSourceDir(file("./src/docs/resources"))
    setBaseDir(file("./src/docs/resources"))
    setOutputDir(file("build/docs"))
    attributes(asciiAttributes)
    options(mapOf("doctype" to "book"))
    isLogDocuments = true

    resources(delegateClosureOf<CopySpec> {
        from("./src/docs/resources/img") {
            include("**/*.png")
        }
        into("./img")
    })

}

tasks.withType(AsciidoctorPdfTask::class) {
    setSourceDir(file("./src/docs"))

    setBaseDir(file("./src/docs/resources"))
    setOutputDir(file("build/pdf"))
    attributes(asciiAttributes)
    options(mapOf("doctype" to "book"))
    isLogDocuments = true
}

tasks.withType(AsciidoctorEditorConfigGenerator::class) {
    setAttributes(asciiAttributes)
    setDestinationDir("./src/docs/resources")
    group = "documentation"
}

tasks.named("processDocsResources") {
    dependsOn("asciidoctorEditorConfig")
}