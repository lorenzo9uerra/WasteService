/* Generated by AN DISI Unibo */ 
package it.unibo.ctxpro_gui
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	QakContext.createContexts(
	        "localhost", this, "wasteservice_req_gui.pl", "sysRules.pl","ctxpro_gui"
	)
}

