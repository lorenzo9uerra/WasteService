/* Generated by AN DISI Unibo */ 
package it.unibo.led

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Led ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "start"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("start") { //this:State
					action { //it:State
						println("	Led | OFF")
						updateResourceRep( "ledState(off)"  
						)
					}
					 transition( edgeName="goto",targetState="listen", cond=doswitch() )
				}	 
				state("listen") { //this:State
					action { //it:State
					}
					 transition(edgeName="t01",targetState="handleSet",cond=whenDispatch("ledSet"))
				}	 
				state("handleSet") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("ledSet(STATE)"), Term.createTerm("ledSet(STATE)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("	Led | ${payloadArg(0).uppercase()}")
								updateResourceRep( "ledState(${payloadArg(0)})"  
								)
						}
					}
					 transition( edgeName="goto",targetState="listen", cond=doswitch() )
				}	 
			}
		}
}
