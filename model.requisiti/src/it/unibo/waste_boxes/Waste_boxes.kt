/* Generated by AN DISI Unibo */ 
package it.unibo.waste_boxes

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Waste_boxes ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "idle"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		
				var ContentGlass = 0.0
				var ContentPlastic = 0.0	
		return { //this:ActionBasciFsm
				state("idle") { //this:State
					action { //it:State
						println("	BOXES: Paper $ContentPlastic, Glass $ContentGlass")
					}
					 transition(edgeName="t01",targetState="handleDeposit",cond=whenDispatch("depositWaste"))
				}	 
				state("handleDeposit") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("depositWaste(MAT,QNT)"), Term.createTerm("depositWaste(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								if(  payloadArg(0) == "glass"  
								 ){ ContentGlass += payloadArg(1).toDouble()  
								}
								else
								 { ContentPlastic += payloadArg(1).toDouble()  
								 }
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
			}
		}
}
