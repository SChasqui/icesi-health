import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.4.10"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.4")
    implementation("org.jetbrains.exposed:exposed:0.8.5")
    implementation(group = "mysql", name = "mysql-connector-java", version = "6.0.6")
    implementation(group = "org.slf4j", name = "slf4j-simple", version = "1.7.25")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.8.1")
    implementation("io.ktor:ktor-gson:$ktor_version")
    testImplementation(group = "junit", name = "junit", version = "4.12")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.ApplicationKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}

