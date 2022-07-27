/* Generated by AN DISI Unibo */ 
package it.unibo.storagemanager

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Storagemanager ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "init"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				var Support = it.unibo.lenziguerra.wasteservice.storage.StorageManagerSupport.getSupport()
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
						println("$Support")
						updateResourceRep( Support.getPrologContent()  
						)
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("idle") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
					}
					 transition(edgeName="t011",targetState="handleAsk",cond=whenRequest("storageAsk"))
					transition(edgeName="t012",targetState="handleDeposit",cond=whenDispatch("storageDeposit"))
				}	 
				state("handleAsk") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("storageAsk(MAT)"), Term.createTerm("storageAsk(MAT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 var SpaceLeft = Support.getSpace(payloadArg(0))  
								answer("storageAsk", "storageAt", "storageAt(${payloadArg(0)},$SpaceLeft)"   )  
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("handleDeposit") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("storageDeposit(MAT,QNT)"), Term.createTerm("storageDeposit(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 Support.deposit(payloadArg(0), payloadArg(1).toFloat())  
								println("$Support")
								updateResourceRep( Support.getPrologContent()  
								)
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
			}
		}
}
