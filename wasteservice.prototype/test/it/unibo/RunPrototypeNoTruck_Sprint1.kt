package it.unibo

import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    QakContext.createContexts(
        "localhost", this, "wasteservice_proto_sprint1_test.pl", "sysRules.pl"
    )
    println("Created context without waste truck")
}
