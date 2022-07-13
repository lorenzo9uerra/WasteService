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
		return "on"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				var Next = 0 
		return { //this:ActionBasciFsm
				state("on") { //this:State
					action { //it:State
						println("	Led | ON")
					}
					 transition(edgeName="t00",targetState="handleStatus",cond=whenEvent("trolleyStatus"))
				}	 
				state("off") { //this:State
					action { //it:State
						println("	Led | OFF")
					}
					 transition(edgeName="t01",targetState="handleStatus",cond=whenEvent("trolleyStatus"))
				}	 
				state("blinking") { //this:State
					action { //it:State
						println("	Led | BLINKING")
					}
					 transition(edgeName="t02",targetState="handleStatus",cond=whenEvent("trolleyStatus"))
				}	 
				state("handleStatus") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("trolleyStatus(STATUS)"), Term.createTerm("trolleyStatus(STATUS)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								
												Next = when(payloadArg(0)) {
													"stopped" -> 0
													"home" -> 1
													else -> 2
												}
						}
					}
					 transition( edgeName="goto",targetState="on", cond=doswitchGuarded({ Next == 1  
					}) )
					transition( edgeName="goto",targetState="elseOffBlink", cond=doswitchGuarded({! ( Next == 1  
					) }) )
				}	 
				state("elseOffBlink") { //this:State
					action { //it:State
					}
					 transition( edgeName="goto",targetState="off", cond=doswitchGuarded({ Next == 0  
					}) )
					transition( edgeName="goto",targetState="blinking", cond=doswitchGuarded({! ( Next == 0  
					) }) )
				}	 
			}
		}
}
