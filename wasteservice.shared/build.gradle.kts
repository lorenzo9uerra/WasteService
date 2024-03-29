import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    `java-library`
    `java-test-fixtures` // Used to share test utilities to other projects
}

val kotlinVersion = "1.6.21"

java.sourceCompatibility = JavaVersion.VERSION_11

version = "1.0"

repositories {
    mavenCentral()
    flatDir { dirs("../unibolibs") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")

    implementation("org.springframework:spring-websocket:5.3.22")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.1-jre")

    /*  MQTT *************************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.eclipse.paho/org.eclipse.paho.client.mqttv3
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    /* JSON **************************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20220320")

    /* COAP **************************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.eclipse.californium/californium-core
    implementation("org.eclipse.californium:californium-core:3.6.0")
    // https://mvnrepository.com/artifact/org.eclipse.californium/californium-proxy2
    implementation("org.eclipse.californium:californium-proxy2:3.6.0")

    // OkHttp library for websockets with Kotlin
    // implementation("com.squareup.okhttp3:okhttp:3.14.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    // https://mvnrepository.com/artifact/com.squareup.okhttp3/mockwebserver
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")

    /* LOG4J *************************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-reload4j
    implementation("org.slf4j:slf4j-reload4j:2.0.0-alpha7")

    /* HTTP **************************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.11.0")

    /* UNIBO *************************************************************************************************************** */
    implementation(":uniboInterfaces")
    implementation(":2p301")
    implementation(":unibo.comm22-1.1")
    // Necessario per utilità messaggi
    implementation(":unibo.qakactor22-3.2")
    implementation(":it.unibo.radarSystem22.domain-2.0")

    /* AIMA **************************************************************************************************************** */
    // PLANNER
    implementation(":unibo.planner22-1.0")
    // https://mvnrepository.com/artifact/com.googlecode.aima-java/aima-core
    implementation("com.googlecode.aima-java:aima-core:3.0.0")

    // Actors for test utilities
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    testFixturesImplementation(":unibo.qakactor22-3.2")
    testFixturesImplementation(":unibo.comm22-1.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
