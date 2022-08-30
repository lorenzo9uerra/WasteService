package it.unibo.local_ctx_wasteservice

import it.unibo.kactor.QakContext
import it.unibo.lenziguerra.wasteservice.utils.FileUtilsWs
import kotlinx.coroutines.runBlocking

fun main() {
    FileUtilsWs.tryExportResource("sysRules.pl")
    FileUtilsWs.tryExportResource("wasteservice.pl")

    runBlocking {
        QakContext.createContexts(
            "localhost", this, "wasteservice.pl", "sysRules.pl"
        )
    }
}