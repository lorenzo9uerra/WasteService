/* Generated by AN DISI Unibo */ 
package it.unibo.trolley

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Trolley ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "idle"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				var CarryingType = ""
				var CarryingAmount = 0.0
				var Pos = arrayOf(0,0)
				fun getContentLine(): String {
					if (CarryingAmount > 0)
						return "\ncontent($CarryingType,$CarryingAmount)"
					else
						return ""
				}
				fun getPosLine(): String {
					return "\npos(${Pos[0]},${Pos[1]})"
				}
		return { //this:ActionBasciFsm
				state("idle") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "state(idle)" + getPosLine() + getContentLine()  
						)
					}
					 transition(edgeName="t010",targetState="handleMove",cond=whenRequest("trolleyMove"))
					transition(edgeName="t011",targetState="handleCollect",cond=whenRequest("trolleyCollect"))
					transition(edgeName="t012",targetState="handleDeposit",cond=whenRequest("trolleyDeposit"))
				}	 
				state("handleMove") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "state(work)" + getPosLine() + getContentLine()  
						)
						delay(700) 
						if( checkMsgContent( Term.createTerm("trolleyMove(X,Y)"), Term.createTerm("trolleyMove(X,Y)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 Pos[0] = payloadArg(0).toInt()  
								 Pos[1] = payloadArg(1).toInt()  
								answer("trolleyMove", "trolleyDone", "trolleyDone(true)"   )  
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("handleCollect") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "state(work)" + getPosLine() + getContentLine()  
						)
						delay(500) 
						if( checkMsgContent( Term.createTerm("trolleyCollect(MAT,QNT)"), Term.createTerm("trolleyCollect(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 CarryingType = payloadArg(0)  
								 CarryingAmount = payloadArg(1).toDouble()  
								answer("trolleyCollect", "trolleyDone", "trolleyDone(true)"   )  
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("handleDeposit") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "state(work)" + getPosLine() + getContentLine()  
						)
						delay(500) 
						forward("storageDeposit", "storageDeposit($CarryingType,$CarryingAmount)" ,"storagemanager" ) 
						 CarryingType = ""  
						 CarryingAmount = 0.0  
						answer("trolleyDeposit", "trolleyDone", "trolleyDone(true)"   )  
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
			}
		}
}
