package it.unibo.lenziguerra.wasteservice.wasteservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WasteserviceApplication

fun main(args: Array<String>) {
	runApplication<WasteserviceApplication>(*args)
}
