/* Generated by AN DISI Unibo */ 
package it.unibo.ctxreq_led
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	QakContext.createContexts(
	        "localhost", this, "wasteservice_req_led.pl", "sysRules.pl","ctxreq_led"
	)
}

