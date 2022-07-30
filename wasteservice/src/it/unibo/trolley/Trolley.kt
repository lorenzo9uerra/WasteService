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
		return "init"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				val Support = it.unibo.lenziguerra.wasteservice.trolley.TrolleySupport.getSupport()
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("idle") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( Support.getPrologContent()  
						)
						println("$Support")
					}
					 transition(edgeName="t08",targetState="handleMove",cond=whenRequest("trolleyMove"))
					transition(edgeName="t09",targetState="handleCollect",cond=whenRequest("trolleyCollect"))
					transition(edgeName="t010",targetState="handleDeposit",cond=whenRequest("trolleyDeposit"))
				}	 
				state("handleMove") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( Support.getPrologContent()  
						)
						if( checkMsgContent( Term.createTerm("trolleyMove(LOC)"), Term.createTerm("trolleyMove(LOC)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								if(  Support.move(payloadArg(0))  
								 ){answer("trolleyMove", "trolleyDone", "trolleyDone(success)"   )  
								 Support.setPosition(payloadArg(0))  
								}
								else
								 {answer("trolleyMove", "trolleyDone", "trolleyDone(fail)"   )  
								 }
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("handleCollect") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( Support.getPrologContent() 
						)
						if( checkMsgContent( Term.createTerm("trolleyCollect(MAT,QNT)"), Term.createTerm("trolleyCollect(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								
												Support.collect(payloadArg(0), payloadArg(1).toFloat())
								answer("trolleyCollect", "trolleyDone", "trolleyDone(success)"   )  
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("handleDeposit") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( Support.getPrologContent() 
						)
						if( checkMsgContent( Term.createTerm("trolleyDeposit(_)"), Term.createTerm("trolleyDeposit(_)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								
												val Material = Support.getMaterial()
												val Quantity = Support.getQuantity()
								forward("storageDeposit", "storageDeposit($Material,$Quantity)" ,"storagemanager" ) 
								answer("trolleyDeposit", "trolleyDone", "trolleyDone(success)"   )  
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
			}
		}
}
