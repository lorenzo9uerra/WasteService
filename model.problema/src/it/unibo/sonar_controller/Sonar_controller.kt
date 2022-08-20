/* Generated by AN DISI Unibo */ 
package it.unibo.sonar_controller

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Sonar_controller ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "init"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
					}
					 transition(edgeName="t00",targetState="stopped",cond=whenEvent("sonarStop"))
					transition(edgeName="t01",targetState="resume",cond=whenEvent("sonarResume"))
				}	 
				state("stopped") { //this:State
					action { //it:State
						println("INVIO STOP")
						forward("trolleyStop", "trolleyStop(_)" ,"trolley" ) 
					}
					 transition( edgeName="goto",targetState="init", cond=doswitch() )
				}	 
				state("resume") { //this:State
					action { //it:State
						println("INVIO RESUME")
						forward("trolleyResume", "trolleyResume(_)" ,"trolley" ) 
					}
					 transition( edgeName="goto",targetState="init", cond=doswitch() )
				}	 
			}
		}
}
