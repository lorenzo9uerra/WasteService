import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    distribution
    application
}

version = "1.0"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    flatDir { dirs("../unibolibs") }
}

dependencies {
    implementation(project(":wasteservice.shared"))

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")

    /* JSON **************************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20220320")

    /* COAP **************************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.eclipse.californium/californium-core
    implementation("org.eclipse.californium:californium-core:3.6.0")
    // https://mvnrepository.com/artifact/org.eclipse.californium/californium-proxy2
    implementation("org.eclipse.californium:californium-proxy2:3.6.0")

    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    /* COROUTINES ********************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")

    /* UNIBO *************************************************************************************************************** */
    implementation(":uniboInterfaces")
    implementation(":2p301")
    implementation(":unibo.qakactor22-3.2")
    implementation(":unibo.comm22-1.1")
    implementation(":it.unibo.radarSystem22.domain-2.0")

    // Actors for test
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    testImplementation(":unibo.qakactor22-3.2")
    testImplementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    testImplementation(testFixtures(project(":wasteservice.shared")))
}

sourceSets["main"].java {
    srcDir("src")
    srcDir("resources")
}

sourceSets["main"].resources { srcDir("resources") }

sourceSets["test"].java { srcDir("test") }

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

// tasks.withType<Test> { useJUnitPlatform() }

application { mainClass.set("it.unibo.lenziguerra.wasteservice.sonar.SonarMainKt") }

task<JavaExec>("runSonarGui") {
    group = "application"
    mainClass.set("it.unibo.lenziguerra.wasteservice.sonar.SonarGuiKt")
    classpath = java.sourceSets["test"].runtimeClasspath
}

task<Jar>("jarSonarGui") {
    group = "build"
    archiveBaseName.set("wasteservice.sonarStopGui")
    manifest.attributes["Main-Class"] = "it.unibo.lenziguerra.wasteservice.sonar.SonarGuiKt"
    manifest.attributes["Class-Path"] = java.sourceSets["test"].runtimeClasspath
}
