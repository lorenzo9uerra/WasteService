package it.unibo.lenziguerra.wasteservice

enum class LedStatus(val id: String) {
    ACCESO("acceso"),
    LAMPEGGIA("lampeggia"),
    SPENTO("spento")
}

enum class WasteType (val id: String) {
    GLASS("glass"),
    PLASTIC("plastic"),
}

enum class TrolleyStatus(val id: String) {
    HOME("home"),
    ATTIVITA("attivita"),
    STOP("stop")
}