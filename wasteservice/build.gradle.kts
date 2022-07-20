import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

group = "it.unibo.lenziguerra.wasteservice"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	flatDir {   dirs("../unibolibs")	 }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-mustache")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework:spring-websocket:5.3.20")
	implementation("org.springframework:spring-messaging:5.3.20")
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	/* COAP **************************************************************************************************************** */
	// https://mvnrepository.com/artifact/org.eclipse.californium/californium-core
	implementation("org.eclipse.californium:californium-core:3.5.0")
	// https://mvnrepository.com/artifact/org.eclipse.californium/californium-proxy2
	implementation("org.eclipse.californium:californium-proxy2:3.5.0")

	/* UNIBO *************************************************************************************************************** */
	implementation(":uniboInterfaces")
	implementation(":2p301")
	implementation(":it.unibo.qakactor-2.7")
	implementation(":unibonoawtsupports")  //required by the old infrastructure
	implementation(":unibo.actor22-1.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

sourceSets["main"].java {
	srcDir("src")
	srcDir("resources")
}
sourceSets["main"].resources {
	srcDir("resources")
}
sourceSets["test"].java {
	srcDir("test")
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
