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
		val interruptedStateTransitions = mutableListOf<Transition>()
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
						stateTimer = TimerActor("timer_init", 
							scope, context!!, "local_tout_trolley_init", 0.toLong() )
					}
					 transition(edgeName="t02",targetState="goingIndoor",cond=whenTimeout("local_tout_trolley_init"))   
					interrupthandle(edgeName="t03",targetState="handleStop",cond=whenDispatch("trolleyStop"),interruptedStateTransitions)
				}	 
				state("goingIndoor") { //this:State
					action { //it:State
						println("Going INDOOR")
						delay(2000) 
					}
					 transition( edgeName="goto",targetState="goingBox", cond=doswitch() )
				}	 
				state("goingBox") { //this:State
					action { //it:State
						println("Going BOX")
						delay(2000) 
					}
					 transition( edgeName="goto",targetState="goingHome", cond=doswitch() )
				}	 
				state("goingHome") { //this:State
					action { //it:State
						println("Going HOME")
						delay(2000) 
					}
					 transition( edgeName="goto",targetState="goingIndoor", cond=doswitch() )
				}	 
				state("exitFromStop") { //this:State
					action { //it:State
						updateResourceRep( "resumed"  
						)
						returnFromInterrupt(interruptedStateTransitions)
					}
				}	 
				state("handleStop") { //this:State
					action { //it:State
						updateResourceRep( "stopped"  
						)
					}
					 transition(edgeName="t04",targetState="exitFromStop",cond=whenDispatch("trolleyResume"))
				}	 
			}
		}
}
