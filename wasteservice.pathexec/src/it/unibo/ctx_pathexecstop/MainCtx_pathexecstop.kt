/* Generated by AN DISI Unibo */ 
package it.unibo.ctx_pathexecstop
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	QakContext.createContexts(
	        "localhost", this, "pathexecstop.pl", "sysRules.pl","ctx_pathexecstop"
	)
}

