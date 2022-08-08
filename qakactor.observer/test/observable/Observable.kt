/* Generated by AN DISI Unibo */ 
package observable

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Observable ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "start"  
						)
					}
					 transition( edgeName="goto",targetState="on", cond=doswitch() )
				}	 
				state("on") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "on"  
						)
						stateTimer = TimerActor("timer_on", 
							scope, context!!, "local_tout_observable_on", 1000.toLong() )
					}
					 transition(edgeName="t00",targetState="off",cond=whenTimeout("local_tout_observable_on"))   
				}	 
				state("off") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						updateResourceRep( "off"  
						)
						stateTimer = TimerActor("timer_off", 
							scope, context!!, "local_tout_observable_off", 1000.toLong() )
					}
					 transition(edgeName="t01",targetState="on",cond=whenTimeout("local_tout_observable_off"))   
				}	 
			}
		}
}
