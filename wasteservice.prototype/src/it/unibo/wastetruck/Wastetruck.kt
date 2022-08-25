/* Generated by AN DISI Unibo */ 
package it.unibo.wastetruck

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Wastetruck ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "init"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
						delay(1000) 
					}
					 transition( edgeName="goto",targetState="start", cond=doswitch() )
				}	 
				state("start") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						request("dopath", "dopath(_)" ,"pathexec" )  
						delay(4000) 
					}
					 transition( edgeName="goto",targetState="start", cond=doswitch() )
				}	 
			}
		}
}
