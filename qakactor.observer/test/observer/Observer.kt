/* Generated by AN DISI Unibo */ 
package observer

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Observer ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						coapObserverUtil.startObserving(myself ,"observable" )
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("idle") { //this:State
					action { //it:State
					}
					 transition(edgeName="t02",targetState="handleUpdate",cond=whenDispatch("coapUpdate"))
				}	 
				state("handleUpdate") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("coapUpdate(VALUE)"), Term.createTerm("coapUpdate(VALUE)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Received COAP update! Value is: ${payloadArg(0)}")
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
			}
		}
}
