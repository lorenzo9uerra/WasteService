package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.lenziguerra.wasteservice.SystemConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WasteserviceApplication

fun main(args: Array<String>) {
	SystemConfig.setConfiguration("SystemConfig.json")

	runApplication<WasteserviceApplication>(*args)
}