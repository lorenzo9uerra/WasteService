/* Generated by AN DISI Unibo */ 
package it.unibo.pathexecstop

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class pathexecstop ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		 var Counter = 0  
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
					}
					 transition(edgeName="t125",targetState="doThePath",cond=whenRequest("dopath"))
					interrupthandle(edgeName="t126",targetState="stopped",cond=whenDispatch("stopPath"),interruptedStateTransitions)
				}	 
				state("doThePath") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						 Counter = 4  
					}
					 transition( edgeName="goto",targetState="nextMove", cond=doswitch() )
				}	 
				state("nextMove") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("pathexecstop | Move progress: ${5 - Counter}")
						 Counter--  
						request("setAlarm", "setAlarm(250)" ,"timer" )  
					}
					 transition(edgeName="t227",targetState="checkWorkEnded",cond=whenReply("triggerAlarm"))
					interrupthandle(edgeName="t228",targetState="stopped",cond=whenDispatch("stopPath"),interruptedStateTransitions)
				}	 
				state("checkWorkEnded") { //this:State
					action { //it:State
					}
					 transition( edgeName="goto",targetState="endWorkOk", cond=doswitchGuarded({ Counter <= 0  
					}) )
					transition( edgeName="goto",targetState="nextMove", cond=doswitchGuarded({! ( Counter <= 0  
					) }) )
				}	 
				state("endWorkOk") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("endWorkOk: PATH DONE")
						answer("dopath", "dopathdone", "dopathdone(ok)"   )  
					}
					 transition( edgeName="goto",targetState="s0", cond=doswitch() )
				}	 
				state("stopped") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("pathexecstop stopped")
					}
					 transition(edgeName="t329",targetState="resumeFromStop",cond=whenDispatch("resumePath"))
				}	 
				state("resumeFromStop") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						println("pathexecstop resumed")
						returnFromInterrupt(interruptedStateTransitions)
					}
				}	 
			}
		}
}
