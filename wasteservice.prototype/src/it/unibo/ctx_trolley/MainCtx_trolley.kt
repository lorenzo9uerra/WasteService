/* Generated by AN DISI Unibo */ 
package it.unibo.ctx_trolley
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	QakContext.createContexts(
	        "localhost", this, "wasteservice_proto_sprint3_contexts.pl", "sysRules.pl","ctx_trolley"
	)
}

