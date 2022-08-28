package it.unibo.lenziguerra.wasteservice.utils

import unibo.comm22.utils.ColorsOut

object LogUtils {
    fun threadOut(s: String, color: String = "") {
        ColorsOut.outappl("${Thread.currentThread().name.padEnd(20)} | $s", color)
    }
    fun threadOutErr(s: String) {
        ColorsOut.outerr("${Thread.currentThread().name.padEnd(20)} | $s")
    }
}