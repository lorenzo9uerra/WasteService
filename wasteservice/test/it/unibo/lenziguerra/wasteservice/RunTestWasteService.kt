package it.unibo.lenziguerra.wasteservice

import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking
import unibo.actor22comm.utils.CommUtils
import kotlin.concurrent.thread

class RunTestWasteServiceKt {
    fun main() = runBlocking {
        thread { QakContext.createContexts(
            "localhost", this, "trolley.pl", "sysRules.pl"
        ) }
        CommUtils.delay(4000)
        thread { QakContext.createContexts(
            "localhost", this, "wasteservice.pl", "sysRules.pl"
        ) }
    }
}
