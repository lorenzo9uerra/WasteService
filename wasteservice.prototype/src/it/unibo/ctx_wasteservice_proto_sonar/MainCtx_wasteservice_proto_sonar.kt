/* Generated by AN DISI Unibo */ 
package it.unibo.ctx_wasteservice_proto_sonar
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	QakContext.createContexts(
	        "localhost", this, "wasteservice_proto_sprint3.pl", "sysRules.pl"
	)
}
