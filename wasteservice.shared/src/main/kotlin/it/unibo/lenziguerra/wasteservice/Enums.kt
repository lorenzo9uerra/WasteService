package it.unibo.lenziguerra.wasteservice

enum class WasteType (val id: String) {
    GLASS("glass"),
    PLASTIC("plastic"),
}

enum class SystemLocation (val isTrashBox: Boolean = false) {
    HOME,
    INDOOR,
    GLASS_BOX(true),
    PLASTIC_BOX(true),
    UNKNOWN,
}

enum class BlinkLedState {
    ON, OFF, BLINKING
}