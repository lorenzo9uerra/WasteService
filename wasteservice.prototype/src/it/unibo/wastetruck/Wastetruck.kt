/* Generated by AN DISI Unibo */ 
package it.unibo.wastetruck

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Wastetruck ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "req"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		
				var Times = 5	
		return { //this:ActionBasciFsm
				state("req") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						
									var Material = if (kotlin.random.Random.nextFloat() > 0.5) "glass" else "plastic"
									var Quantity = kotlin.random.Random.nextInt(10, 30)	
						println("	Truck with $Material in amount $Quantity arrived")
						request("loadDeposit", "loadDeposit($Material,$Quantity)" ,"wasteservice" )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t122",targetState="handleAccepted",cond=whenReply("loadaccept"))
					transition(edgeName="t123",targetState="handleRejected",cond=whenReply("loadrejected"))
				}	 
				state("handleRejected") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	Truck denied")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="waitArrival", cond=doswitch() )
				}	 
				state("handleAccepted") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	Truck accepted")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t024",targetState="waitArrival",cond=whenDispatch("pickedUp"))
				}	 
				state("waitArrival") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						 var DelayTime : kotlin.Long = kotlin.random.Random.nextLong(500, 10000)  
						delay(DelayTime)
						 Times--  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="req", cond=doswitchGuarded({ Times > 0  
					}) )
					transition( edgeName="goto",targetState="finish", cond=doswitchGuarded({! ( Times > 0  
					) }) )
				}	 
				state("finish") { //this:State
					action { //it:State
						println("	Termine simulazione Qak")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
				}	 
			}
		}
}
