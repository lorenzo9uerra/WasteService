package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.utils.StaticConfig

object SystemConfig {
    var debugPrint = true

    var DLIMIT = 50
    var positions = mutableMapOf(
        "home" to arrayOf(arrayOf(0, 0), arrayOf(0, 0)),
        "indoor" to arrayOf(arrayOf(0, 5), arrayOf(1, 5)),
        "plastic_box" to arrayOf(arrayOf(4, 0), arrayOf(5, 0)),
        "glass_box" to arrayOf(arrayOf(4, 5), arrayOf(5, 5)),
    )

    var ports =
        mutableMapOf(
            "trolley" to 8022,
            "storage" to 8021,
            "wasteServiceServer" to 8080,
            "wasteServiceContext" to 8023,
            "pathexec" to 8020
        )
    var hosts = mutableMapOf(
        "trolley" to "localhost",
        "storage" to "localhost",
        "wasteServiceServer" to "localhost",
        "wasteServiceContext" to "localhost",
        "pathexec" to "localhost"
    )

    fun setConfiguration(cfgPath: String) {
        StaticConfig.setConfiguration(this::class, cfgPath)
    }
}