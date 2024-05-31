plugins {
    id("common")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    applicationName = "dp-regel-periode"
    mainClass.set("no.nav.dagpenger.regel.periode.ApplicationKt")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.rapids.and.rivers)

    implementation("com.github.navikt:dp-inntekt-kontrakter:1_20231220.55a8a9")
    implementation("com.github.navikt:dagpenger-events:20240311.fa98eb")
    implementation("com.github.navikt:dp-grunnbelop:2024.05.30-13.38.6e9169eb05d1")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    // prometheus
    implementation("io.prometheus:simpleclient_common:0.16.0")
    implementation("io.prometheus:simpleclient_hotspot:0.16.0")
    implementation("no.nav:nare-prometheus:0b41ab4")

    implementation(libs.kotlin.logging)

    implementation("no.nav:nare:768ae37")

    // Miljøkonfigurasjon
    implementation(libs.konfig)

    // unleash
    implementation("io.getunleash:unleash-client-java:9.2.2")

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions.core)
    testImplementation("io.kotest:kotest-assertions-json-jvm:${libs.versions.kotest.get()}")
    testImplementation(libs.mockk)
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}
