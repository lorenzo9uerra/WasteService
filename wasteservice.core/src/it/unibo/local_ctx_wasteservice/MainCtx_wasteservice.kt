package it.unibo.local_ctx_wasteservice

import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    QakContext.createContexts(
        "localhost", this, "wasteservice.pl", "sysRules.pl"
    )
}