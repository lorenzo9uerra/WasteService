/* Generated by AN DISI Unibo */ 
package it.unibo.wasteservice_req

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Wasteservice_req ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "idle"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		
		  var CurrentRequestMaterial = ""
		  var CurrentRequestQuantity = 0.0
		  var CurrentRequestCheck = 0.0
		  var CurrentSpace = 0.0
		return { //this:ActionBasciFsm
				state("idle") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
					}
					 transition(edgeName="tIdle2",targetState="handleRequest",cond=whenRequest("loadDeposit"))
				}	 
				state("handleRequest") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("loadDeposit(MAT,QNT)"), Term.createTerm("loadDeposit(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 
								  				CurrentRequestMaterial = payloadArg(0)
								  				CurrentRequestQuantity = payloadArg(1).toDouble()
								  				CurrentRequestCheck = CurrentRequestQuantity
								 CurrentSpace = kotlin.random.Random.Default.nextDouble(0.0, 50.0)  
								println("	WS | Request received $CurrentRequestMaterial $CurrentRequestQuantity, has $CurrentSpace")
								if(  CurrentRequestCheck > CurrentSpace  
								 ){answer("loadDeposit", "loadrejected", "loadrejected(_)"   )  
								}
								else
								 {answer("loadDeposit", "loadaccept", "loadaccept(_)"   )  
								 }
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
			}
		}
}
