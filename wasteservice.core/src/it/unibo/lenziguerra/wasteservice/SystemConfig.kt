package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.utils.StaticConfig

object SystemConfig {
    var debugPrint = true

    var DLIMIT = 50
    var positions = mutableMapOf(
        "home" to listOf(listOf(0, 0), listOf(0, 0)),
        "indoor" to listOf(listOf(0, 5), listOf(1, 5)),
        "plastic_box" to listOf(listOf(4, 0), listOf(5, 0)),
        "glass_box" to listOf(listOf(4, 5), listOf(5, 5)),
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

    private var setConf = false

    fun setConfiguration(cfgPath: String = "SystemConfig.json", force: Boolean = false) {
        if (!setConf || force) {
            StaticConfig.setConfiguration(this::class, this, cfgPath)
            setConf = true
        }
    }

}
