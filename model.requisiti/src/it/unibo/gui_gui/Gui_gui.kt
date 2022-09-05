/* Generated by AN DISI Unibo */ 
package it.unibo.gui_gui

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Gui_gui ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "show"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		
				var TrolleyPos = ""
				var TrolleyStatus = ""
				var LedStatus =	""
		return { //this:ActionBasciFsm
				state("show") { //this:State
					action { //it:State
						println("	GUI: Trolley [Position: $TrolleyPos, Status: $TrolleyStatus], Led [$LedStatus]")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t00",targetState="updateLed",cond=whenEvent("ledStatus"))
					transition(edgeName="t01",targetState="updateTrolley",cond=whenEvent("trolleyStatus"))
				}	 
				state("updateLed") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("ledStatus(STATUS)"), Term.createTerm("ledStatus(STATUS)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 LedStatus = payloadArg(0)  
						}
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="show", cond=doswitch() )
				}	 
				state("updateTrolley") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("trolleyStatus(STATUS,POS)"), Term.createTerm("trolleyStatus(STATUS,POS)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 TrolleyStatus = payloadArg(0)  
								 TrolleyPos    = payloadArg(1)  
						}
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="show", cond=doswitch() )
				}	 
			}
		}
}
