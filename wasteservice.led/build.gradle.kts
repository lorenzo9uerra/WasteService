import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.6.21"
}

repositories {
	mavenCentral()
	flatDir {   dirs("../unibolibs")	 }
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
	implementation("org.eclipse.californium:californium-core:3.5.0")
	// https://mvnrepository.com/artifact/org.eclipse.californium/californium-proxy2
	implementation("org.eclipse.californium:californium-proxy2:3.5.0")

	/* UNIBO *************************************************************************************************************** */
    implementation(":uniboInterfaces")
    implementation(":2p301")
    implementation(":unibo.comm22-1.1")
	implementation(":it.unibo.radarSystem22.domain-1.0")
}