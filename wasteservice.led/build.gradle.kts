plugins {
	kotlin("jvm") version "1.6.21"
	distribution
	application
}

version = "1.0"

java.sourceCompatibility = JavaVersion.VERSION_11

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
	implementation("org.eclipse.californium:californium-core:3.6.0")
	// https://mvnrepository.com/artifact/org.eclipse.californium/californium-proxy2
	implementation("org.eclipse.californium:californium-proxy2:3.6.0")

	/* UNIBO *************************************************************************************************************** */
    implementation(":uniboInterfaces")
    implementation(":2p301")
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

application {
	mainClass.set("it.unibo.lenziguerra.wasteservice.led.LedContainerKt")
}
