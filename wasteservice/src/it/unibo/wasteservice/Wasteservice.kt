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
		return "start"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				val Support = it.unibo.lenziguerra.wasteservice.wasteservice.WasteserviceSupport()
				var Material = ""
				var Quantity = 0.0f
				var Box = ""
				var Position = "x0y0"
		return { //this:ActionBasciFsm
				state("start") { //this:State
					action { //it:State
						println("	WS | Start")
					}
					 transition( edgeName="goto",targetState="home", cond=doswitch() )
				}	 
				state("home") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "tpos(home)"  
						)
						println("	WS | Trolley at home")
					}
					 transition(edgeName="t00",targetState="go_indoor",cond=whenRequest("triggerDeposit"))
				}	 
				state("go_indoor") { //this:State
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
					 transition(edgeName="t21",targetState="indoor",cond=whenReply("trolleyDone"))
				}	 
				state("indoor") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	WT | Trolley at indoor, picking up $Quantity $Material...")
						updateResourceRep( "tpos(indoor)"  
						)
						request("trolleyCollect", "trolleyCollect($Material,$Quantity)" ,"trolley" )  
					}
					 transition(edgeName="t32",targetState="go_box",cond=whenReply("trolleyDone"))
				}	 
				state("go_box") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						answer("triggerDeposit", "trolleyPickedUp", "trolleyPickedUp(_)"   )  
						 Position = Support.getDestination(Box, Position)  
						request("trolleyMove", "trolleyMove($Position)" ,"trolley" )  
					}
					 transition(edgeName="t43",targetState="box",cond=whenReply("trolleyDone"))
				}	 
				state("box") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	WT | Trolley at $Material box, depositing $Quantity $Material...")
						updateResourceRep( "tpos(" + Material + "_box)"  
						)
						request("trolleyDeposit", "trolleyDeposit(_)" ,"trolley" )  
					}
					 transition(edgeName="t54",targetState="done",cond=whenReply("trolleyDone"))
				}	 
				state("done") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	WT | Done deposit action")
						stateTimer = TimerActor("timer_done", 
							scope, context!!, "local_tout_wasteservice_done", 0.toLong() )
					}
					 transition(edgeName="t05",targetState="go_home",cond=whenTimeout("local_tout_wasteservice_done"))   
					transition(edgeName="t06",targetState="go_indoor",cond=whenRequest("triggerDeposit"))
				}	 
				state("go_home") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						 Position = Support.getDestination("home", Position)  
						request("trolleyMove", "trolleyMove($Position)" ,"trolley" )  
					}
					 transition(edgeName="t77",targetState="home",cond=whenReply("trolleyDone"))
				}	 
			}
		}
}
