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
        jcenter()
    }
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    mavenCentral()
    jcenter()
    maven("http://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

application {
    applicationName = "dp-regel-periode"
    mainClassName = "no.nav.dagpenger.regel.periode.PeriodeKt"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Multi-Release"] = "true" // https://github.com/johnrengelman/shadow/issues/449
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Dagpenger.Streams)
    implementation(Dagpenger.Events)
    implementation(Dagpenger.Biblioteker.grunnbeløp)

    implementation(Moshi.moshi)
    implementation(Moshi.moshiAdapters)
    implementation(Moshi.moshiKotlin)
    implementation(Moshi.moshiKtor)

    implementation(Ulid.ulid)

    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.log4j2)
    implementation(Prometheus.Nare.prometheus)

    implementation(Nare.nare)

    implementation(Ktor.serverNetty)
    implementation(Konfig.konfig)
    implementation(Kafka.clients)
    implementation(Kafka.streams)

    implementation(Log4j2.api)
    implementation(Log4j2.core)
    implementation(Log4j2.slf4j)
    implementation(Log4j2.Logstash.logstashLayout)
    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(kotlin("test"))
    testImplementation(Junit5.api)
    testRuntimeOnly(Junit5.engine)
    testRuntimeOnly(Junit5.vintageEngine)
    testImplementation(Kafka.streamTestUtils)
    testImplementation(KafkaEmbedded.env)
    testImplementation(Wiremock.standalone)
    testImplementation(Mockk.mockk)
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt*")
        ktlint()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.named("compileKotlin") {
    dependsOn("spotlessKotlinCheck")
}
