import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.3"
	id("io.spring.dependency-management") version "1.0.13.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	distribution
	application
}

group = "unibo.lenziguerra"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	flatDir {   dirs("../unibolibs")	 }
}

dependencies {
	implementation(project(":wasteservice.shared"))
	implementation(project(":wasteservice.led"))
	implementation(project(":wasteservice.sonar"))

	implementation("org.springframework.boot:spring-boot-starter-mustache")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework:spring-websocket:5.3.22")
	implementation("org.springframework:spring-messaging:5.3.22")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")

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

	implementation(":uniboInterfaces")
    implementation(":2p301")
    implementation(":unibo.comm22-1.1")
	implementation(":unibo.qakactor22-3.2")
	implementation(":it.unibo.radarSystem22.domain-2.0")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
