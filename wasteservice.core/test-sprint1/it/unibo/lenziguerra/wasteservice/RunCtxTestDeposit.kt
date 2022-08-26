package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking

class RunCtxTestDeposit {
    fun main() = runBlocking {
        QakContext.createContexts(
            "localhost", this, "wasteservice.pl", "sysRules.pl"
        )
    }
}
