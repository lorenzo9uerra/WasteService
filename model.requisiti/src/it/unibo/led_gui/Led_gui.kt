/* Generated by AN DISI Unibo */ 
package it.unibo.led_gui

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Led_gui ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "wait"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		return { //this:ActionBasciFsm
				state("wait") { //this:State
					action { //it:State
						delay(1500) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="randomStatus", cond=doswitch() )
				}	 
				state("randomStatus") { //this:State
					action { //it:State
						
									var Status = ""
						
									var r = kotlin.random.Random.nextFloat() 
									if (r < 0.33) {
										Status = "Off"
									} else if (r < 0.66) {
										Status = "On"
									} else {
										Status = "Blinking"
									}
						emit("ledStatus", "ledStatus($Status)" ) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="wait", cond=doswitch() )
				}	 
			}
		}
}
