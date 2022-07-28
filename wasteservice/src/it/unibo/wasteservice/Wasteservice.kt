/* Generated by AN DISI Unibo */ 
package it.unibo.wasteservice

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Wasteservice ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "init"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				val Support = it.unibo.lenziguerra.wasteservice.wasteservice.WasteserviceSupport()
				var Material = ""
				var Quantity = 0.0f
				var Box = ""
				var Position = "x0y0"
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
						println("Start")
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("idle") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
					}
					 transition(edgeName="t00",targetState="moveTrolleyIndoor",cond=whenRequest("triggerDeposit"))
				}	 
				state("moveTrolleyIndoor") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("triggerDeposit(MAT,QNT)"), Term.createTerm("triggerDeposit(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								
												Material = payloadArg(0)
												Quantity = payloadArg(1).toFloat()
												Box = Material + "_box"
						}
						 Position = Support.getDestination("indoor", Position)  
						request("trolleyMove", "trolleyMove($Position)" ,"trolley" )  
					}
					 transition(edgeName="t21",targetState="makeTrolleyCollect",cond=whenReply("trolleyDone"))
				}	 
				state("makeTrolleyCollect") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						request("trolleyCollect", "trolleyCollect($Material,$Quantity)" ,"trolley" )  
					}
					 transition(edgeName="t32",targetState="moveTrolleyDeposit",cond=whenReply("trolleyDone"))
				}	 
				state("moveTrolleyDeposit") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						answer("triggerDeposit", "trolleyPickedUp", "trolleyPickedUp(_)"   )  
						 Position = Support.getDestination(Box, Position)  
						request("trolleyMove", "trolleyMove($Position)" ,"trolley" )  
					}
					 transition(edgeName="t43",targetState="makeTrolleyDeposit",cond=whenReply("trolleyDone"))
				}	 
				state("makeTrolleyDeposit") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						request("trolleyDeposit", "trolleyDeposit(_)" ,"trolley" )  
					}
					 transition(edgeName="t54",targetState="moveToHome",cond=whenReply("trolleyDone"))
				}	 
				state("waitTrolleyDone") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
					}
					 transition(edgeName="t65",targetState="moveToHome",cond=whenReply("trolleyDone"))
				}	 
				state("moveToHome") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						 Position = Support.getDestination("home", Position)  
						request("trolleyMove", "trolleyMove($Position)" ,"trolley" )  
					}
					 transition(edgeName="t76",targetState="idle",cond=whenReply("trolleyDone"))
				}	 
			}
		}
}
