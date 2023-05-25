import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
    id(Shadow.shadow) version Shadow.version
}

buildscript {
    repositories {
        mavenCentral()
    }
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

application {
    applicationName = "dp-regel-periode"
    mainClass.set("no.nav.dagpenger.regel.periode.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Multi-Release"] = "true" // https://github.com/johnrengelman/shadow/issues/449
    }
}
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Dagpenger.Streams)
    implementation(Dagpenger.Events)
    implementation("com.github.navikt:dp-grunnbelop:2023.05.24-15.26.f42064d9fdc8")

    implementation(Moshi.moshi)
    implementation(Moshi.moshiAdapters)
    implementation(Moshi.moshiKotlin)

    implementation(Ulid.ulid)

    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.log4j2)
    implementation(Prometheus.Nare.prometheus)

    implementation(Log4j2.api)
    implementation(Log4j2.core)
    implementation(Log4j2.slf4j)
    implementation(Log4j2.library("layout-template-json"))

    implementation(Nare.nare)

    implementation(Konfig.konfig)
    implementation(Kafka.clients)
    implementation(Kafka.streams)

    implementation(Kotlin.Logging.kotlinLogging)

    implementation("io.getunleash:unleash-client-java:8.0.0")

    testImplementation(kotlin("test"))
    testImplementation(Junit5.api)
    testImplementation(Junit5.params)
    testRuntimeOnly(Junit5.engine)

    testImplementation(KoTest.runner)
    testImplementation(KoTest.assertions)

    testImplementation(Kafka.streamTestUtils)
    testImplementation(Wiremock.standalone)
    testImplementation(Mockk.mockk)
}

spotless {
    kotlin {
        ktlint(Ktlint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt*")
        ktlint(Ktlint.version)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        showStandardStreams = true
    }
}

tasks.named("compileKotlin") {
    dependsOn("spotlessKotlinCheck")
}
