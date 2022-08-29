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
		val interruptedStateTransitions = mutableListOf<Transition>()
		
			// Semplificato, usando singola coordinata invece che area
			var POS_HOME = arrayOf(0,0)
			var POS_INDOOR = arrayOf(0,5)
			var POS_PLASTIC_BOX = arrayOf(5,2)
			var POS_GLASS_BOX = arrayOf(3,0)
			
			var CurrentType = ""
			var CurrentAmount = 0.0
		  	var CurrentRequestPass = false
		return { //this:ActionBasciFsm
				state("start") { //this:State
					action { //it:State
						updateResourceRep( "tpos(home)"  
						)
						println("	WS | Start, trolley at home")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="waitRequest", cond=doswitch() )
				}	 
				state("waitRequest") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="tIdle3",targetState="handleRequest",cond=whenRequest("loadDeposit"))
				}	 
				state("handleRequest") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("loadDeposit(MAT,QNT)"), Term.createTerm("loadDeposit(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 
								  				CurrentType = payloadArg(0)
								  				CurrentAmount = payloadArg(1).toDouble()
								println("	WS | Request received $CurrentType $CurrentAmount")
								request("storageAsk", "storageAsk($CurrentType)" ,"storagemanager" )  
						}
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t04",targetState="handleStorageReply",cond=whenReply("storageAt"))
				}	 
				state("handleStorageReply") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						 CurrentRequestPass = false  
						if( checkMsgContent( Term.createTerm("storageAt(MAT,QNT)"), Term.createTerm("storageAt(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("	WS | Has space: ${payloadArg(1)} for ${payloadArg(0)}")
								 CurrentRequestPass = CurrentAmount <= payloadArg(1).toDouble()  
						}
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="go_indoor", cond=doswitchGuarded({ CurrentRequestPass  
					}) )
					transition( edgeName="goto",targetState="rejectRequest", cond=doswitchGuarded({! ( CurrentRequestPass  
					) }) )
				}	 
				state("rejectRequest") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	WS | rejected")
						answer("loadDeposit", "loadrejected", "loadrejected(_)"   )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="waitRequest", cond=doswitch() )
				}	 
				state("go_indoor") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	WS | Enough space, sending trolley...")
						var X = POS_INDOOR[0]; var Y = POS_INDOOR[1] 
						request("trolleyMove", "trolleyMove($X,$Y)" ,"trolley" )  
						answer("loadDeposit", "loadaccept", "loadaccept(_)"   )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t05",targetState="indoor",cond=whenReply("trolleyDone"))
				}	 
				state("indoor") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "tpos(indoor)"  
						)
						println("	WT | Trolley at indoor, picking up $CurrentAmount $CurrentType...")
						request("trolleyCollect", "trolleyCollect($CurrentType,$CurrentAmount)" ,"trolley" )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t06",targetState="go_box",cond=whenReply("trolleyDone"))
				}	 
				state("go_box") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						forward("pickedUp", "pickedUp(_)" ,"wastetruck" ) 
						var X = POS_GLASS_BOX[0]; var Y = POS_GLASS_BOX[1] 
						if(  CurrentType == "plastic"  
						 ){X = POS_PLASTIC_BOX[0]; Y = POS_PLASTIC_BOX[1] 
						}
						request("trolleyMove", "trolleyMove($X,$Y)" ,"trolley" )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t07",targetState="box",cond=whenReply("trolleyDone"))
				}	 
				state("box") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	WT | Trolley at $CurrentType box, depositing $CurrentAmount $CurrentType...")
						updateResourceRep( "tpos(" + CurrentType + "_box)"  
						)
						request("trolleyDeposit", "trolleyDeposit(_)" ,"trolley" )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t08",targetState="done",cond=whenReply("trolleyDone"))
				}	 
				state("done") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("	WT | Done deposit action")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
				 	 		//sysaction { //it:State
				 	 		  stateTimer = TimerActor("timer_done", 
				 	 			scope, context!!, "local_tout_wasteservice_done", 0.toLong() )
				 	 		//}
					}	 	 
					 transition(edgeName="t09",targetState="go_home",cond=whenTimeout("local_tout_wasteservice_done"))   
					transition(edgeName="t010",targetState="handleSecondRequest",cond=whenRequest("loadDeposit"))
				}	 
				state("handleSecondRequest") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("loadDeposit(MAT,QNT)"), Term.createTerm("loadDeposit(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 
								  				CurrentType = payloadArg(0)
								  				CurrentAmount = payloadArg(1).toDouble()
								println("	WS | Another request received $CurrentType $CurrentAmount")
								request("storageAsk", "storageAsk($CurrentType)" ,"storagemanager" )  
						}
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t011",targetState="handleSecondStorageReply",cond=whenReply("storageAt"))
				}	 
				state("handleSecondStorageReply") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						 CurrentRequestPass = false  
						if( checkMsgContent( Term.createTerm("storageAt(MAT,QNT)"), Term.createTerm("storageAt(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("	WS | Has space: ${payloadArg(1)} for ${payloadArg(0)}")
								 CurrentRequestPass = CurrentAmount <= payloadArg(1).toDouble()  
						}
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="go_indoor", cond=doswitchGuarded({ CurrentRequestPass  
					}) )
					transition( edgeName="goto",targetState="rejectSecondRequest", cond=doswitchGuarded({! ( CurrentRequestPass  
					) }) )
				}	 
				state("rejectSecondRequest") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						answer("loadDeposit", "loadrejected", "loadrejected(_)"   )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="go_home", cond=doswitch() )
				}	 
				state("go_home") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						var X = POS_HOME[0]; var Y = POS_HOME[1] 
						request("trolleyMove", "trolleyMove($X,$Y)" ,"trolley" )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t012",targetState="home",cond=whenReply("trolleyDone"))
				}	 
				state("home") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "tpos(home)"  
						)
						println("	WS | Trolley at home")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="waitRequest", cond=doswitch() )
				}	 
			}
		}
}
