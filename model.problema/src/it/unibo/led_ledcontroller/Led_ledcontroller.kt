/* Generated by AN DISI Unibo */ 
package it.unibo.led_ledcontroller

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Led_ledcontroller ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "init"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
						coapObserverUtil.startObserving(myself ,"led_trolley" )
					}
					 transition( edgeName="goto",targetState="observe", cond=doswitch() )
				}	 
				state("observe") { //this:State
					action { //it:State
					}
					 transition(edgeName="t00",targetState="handleStatus",cond=whenDispatch("coapUpdate"))
				}	 
				state("handleStatus") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("coapUpdate(RESOURCE,VALUE)"), Term.createTerm("coapUpdate(RESOURCE,VALUE)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Led Controller | Received update ${payloadArg(0)}")
								
												val Next = when(payloadArg(0)) {
													"stopped" -> "off"
													"home" -> "on"
													else -> "blinking"
												}
								println("Led Controller | Setting led to $Next")
								forward("ledSet", "ledSet($Next)" ,"led_led" ) 
						}
					}
					 transition( edgeName="goto",targetState="observe", cond=doswitch() )
				}	 
			}
		}
}