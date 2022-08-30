package it.unibo.lenziguerra.wasteservice.utils

import org.apache.commons.io.FileUtils
import unibo.comm22.utils.ColorsOut
import java.io.File
import java.io.FileWriter
import java.net.URL
import kotlin.io.path.Path


object LogUtils {
    fun threadOut(s: String, color: String = "") {
        ColorsOut.outappl("${Thread.currentThread().name.padEnd(20)} | $s", color)
    }
    fun threadOutErr(s: String) {
        ColorsOut.outerr("${Thread.currentThread().name.padEnd(20)} | $s")
    }
}

object FileUtilsWs {
    fun createTextIfNotExists(path: String, defaultContent: String) {
        val file = Path(path)
        if (!file.toFile().exists()) {
            println("$path doesn't exist, creating default...")
            FileWriter(file.toAbsolutePath().toString()).use {
                it.write(defaultContent)
            }
        }
    }

    fun tryExportResource(resourceName: String, destName: String = resourceName) {
        val inputUrl: URL = javaClass.classLoader.getResource(resourceName) ?:
            throw IllegalArgumentException("Unknown resource $resourceName")
        val dest = File("./$destName")
        if (!dest.exists()) {
            println("$destName doesn't exist, creating default from resource $resourceName...")
            FileUtils.copyURLToFile(inputUrl, dest)
        }
    }
}