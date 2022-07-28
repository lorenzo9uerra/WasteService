package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.utils.StaticConfig

object SystemConfig {
    var debugPrint = true

    var DLIMIT = 50
    var positions = mutableMapOf(
        "home" to arrayOf(arrayOf(0,0), arrayOf(0,0)),
        "indoor" to arrayOf(arrayOf(0,5), arrayOf(1,5)),
        "plastic_box" to arrayOf(arrayOf(4,0), arrayOf(5,0)),
        "glass_box" to arrayOf(arrayOf(4,5), arrayOf(5,5)),
    )

    var wasteServiceHost = "localhost"
    var wasteServiceServerPort = 8080
    var wasteServiceContextPort = 8023
    var storageHost = "localhost"
    var storagePort = 8021
    var trolleyHost = "localhost"
    var trolleyPort = 8022

    fun setConfiguration(cfgPath: String) {
        StaticConfig.setConfiguration(this::class, cfgPath)
    }
}