package it.unibo.lenziguerra.wasteservice

import it.unibo.lenziguerra.wasteservice.utils.StaticConfig

object SystemConfig {
    var debugPrint = true

    var DLIMIT = 50
    var positions = mutableMapOf(
        "home" to listOf(listOf(0, 0), listOf(0, 0)),
        "indoor" to listOf(listOf(0, 4), listOf(1, 4)),
        "plastic_box" to listOf(listOf(4, 1), listOf(4, 2)),
        "glass_box" to listOf(listOf(3, 4), listOf(4, 4)),
    )

    var ports = mutableMapOf(
        "trolley" to 8023,
        "storage" to 8023,
        "wasteServiceServer" to 8080,
        "wasteServiceContext" to 8023,
        "led" to 8030,
        "sonar" to 8031,
        "sonarinterrupter" to 8023,
        "basicrobot" to 8020,
        "pathexecstop" to 8025,
    )
    var hosts = mutableMapOf(
        "trolley" to "localhost",
        "storage" to "localhost",
        "wasteServiceServer" to "localhost",
        "wasteServiceContext" to "localhost",
        "led" to "localhost",
        "sonar" to "localhost",
        "sonarinterrupter" to "localhost",
        "basicrobot" to "localhost",
        "pathexecstop" to "localhost",
    )
    var actors = mutableMapOf(
        "trolley" to "trolley",
        "storage" to "storagemanager",
        "wasteServiceContext" to "wasteservice",
        "pathexecstop" to "pathexecstop",
        "sonarinterrupter" to "sonarinterrupter",
        "sonar" to "sonarshim",
    )
    var contexts = mutableMapOf(
        "trolley" to "ctx_wasteservice",
        "storage" to "ctx_wasteservice",
        "wasteServiceContext" to "ctx_wasteservice",
        "pathexecstop" to "ctx_pathexecstop",
        "sonarinterrupter" to "ctx_wasteservice",
        "sonar" to "ctx_raspberry_sonar",
    )

    private var setConf = false
    private var noWrite = false
    private var noRead = false

    fun setConfiguration(cfgPath: String = "SystemConfig.json", force: Boolean = false) {
        if ((!setConf || force) && !noRead) {
            StaticConfig.setConfiguration(this::class, this, cfgPath, noWrite)
            setConf = true
        }
    }

    // For testing
    fun disableWrite() {
        noWrite = true
    }
    fun enableWrite() {
        noWrite = false
    }
    fun disableRead() {
        noRead = true
    }
    fun enableRead() {
        noRead = false
    }
}
