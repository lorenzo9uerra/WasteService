/* Generated by AN DISI Unibo */ 
package it.unibo.trolley_dep

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Trolley_dep ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "home"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		
				var CarryingType = ""
				var CarryingAmount = 0.0
		return { //this:ActionBasciFsm
				state("home") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	Trolley | At home")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t00",targetState="go_indoor",cond=whenDispatch("testDeposit"))
				}	 
				state("go_indoor") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("testDeposit(MAT,QNT)"), Term.createTerm("testDeposit(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								
											CarryingType = payloadArg(0)
											CarryingAmount = payloadArg(1).toDouble()	
						}
						delay(500) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="indoor", cond=doswitch() )
				}	 
				state("indoor") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	Trolley | At indoor, picking up $CarryingAmount $CarryingType...")
						delay(500) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="go_box", cond=doswitch() )
				}	 
				state("go_box") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						delay(500) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="box", cond=doswitch() )
				}	 
				state("box") { //this:State
					action { //it:State
						println("	Trolley | At $CarryingType box, depositing $CarryingAmount $CarryingType...")
						delay(200) 
						forward("depositWaste", "depositWaste($CarryingType,$CarryingAmount)" ,"waste_boxes" ) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="done", cond=doswitch() )
				}	 
				state("done") { //this:State
					action { //it:State
						println("	Trolley | Done deposit action")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
				}	 
			}
		}
}
