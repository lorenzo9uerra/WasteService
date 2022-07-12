package it.unibo

import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking
import java.nio.file.Paths

fun main() = runBlocking {
    // Facciamo noi le vedi di WasteTruck
    val plFilePath = Paths.get("test", "wasteservice_req_request_no_truck.pl").toAbsolutePath().toString();
    QakContext.createContexts(
        "localhost", this, plFilePath, "sysRules.pl"
    )
}