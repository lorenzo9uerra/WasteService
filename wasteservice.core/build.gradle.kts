import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    distribution
    application
}

val kotlinVersion = "1.6.21"

group = "it.unibo.lenziguerra.wasteservice"

version = "1.0"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    flatDir { dirs("../unibolibs") }
}

dependencies {
    implementation(project(":wasteservice.shared"))
    testImplementation(testFixtures(project(":wasteservice.shared")))

    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework:spring-websocket:5.3.22")
    implementation("org.springframework:spring-messaging:5.3.22")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.1-jre")

    // Use the Kotlin test library.
    //	testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    //	testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    /* COROUTINES ********************************************************************************************************** */
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")

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
    implementation(":unibo.qakactor22-3.2")
    implementation(":it.unibo.radarSystem22.domain-2.0")
    implementation(":unibo.comm22-1.1")

    /* AIMA **************************************************************************************************************** */
    // PLANNER
    implementation(":unibo.planner22-1.0")
    // https://mvnrepository.com/artifact/com.googlecode.aima-java/aima-core
    implementation("com.googlecode.aima-java:aima-core:3.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("com.vaadin.external.google", "android-json")
    }
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

tasks.withType<Test> { useJUnitPlatform() }

application { mainClass.set("it.unibo.local_ctx_wasteservice.MainCtx_wasteserviceKt") }

tasks.bootRun {
    mainClass.set("it.unibo.lenziguerra.wasteservice.wasteservice.WasteserviceApplicationKt")
}

tasks.bootJar {
    mainClass.set("it.unibo.lenziguerra.wasteservice.wasteservice.WasteserviceApplicationKt")
}

task<JavaExec>("storageCtxRun") {
    group = "qakctx"
    mainClass.set("it.unibo.ctx_storagemanager.MainCtx_storagemanagerKt")
    classpath = java.sourceSets["main"].runtimeClasspath
}

task<JavaExec>("wasteserviceCtxRun") {
    group = "qakctx"
    mainClass.set("it.unibo.ctx_wasteservice.MainCtx_wasteserviceKt")
    classpath = java.sourceSets["main"].runtimeClasspath
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        windowsScript.writeText(windowsScript.readText().replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*"))
    }
}