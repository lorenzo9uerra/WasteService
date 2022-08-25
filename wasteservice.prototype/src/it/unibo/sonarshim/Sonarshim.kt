/* Generated by AN DISI Unibo */ 
package it.unibo.sonarshim

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Sonarshim ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "scanWait"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		
				var Val = 200
				var Wait = 2000L
		return { //this:ActionBasciFsm
				state("scanWait") { //this:State
					action { //it:State
						stateTimer = TimerActor("timer_scanWait", 
							scope, context!!, "local_tout_sonarshim_scanWait", Wait )
					}
					 transition(edgeName="t00",targetState="scan",cond=whenTimeout("local_tout_sonarshim_scanWait"))   
				}	 
				state("scan") { //this:State
					action { //it:State
						 
									var PrevVal = Val
									Val = 200 - Val
						if(  PrevVal != Val  
						 ){println("	Sonar: detected distance $Val")
						if(  PrevVal > 100  
						 ){forward("stopPath", "stopPath(_)" ,"pathexec" ) 
						}
						else
						 {forward("resumePath", "resumePath(_)" ,"pathexec" ) 
						 }
						updateResourceRep( "$Val"  
						)
						}
					}
					 transition( edgeName="goto",targetState="scanWait", cond=doswitch() )
				}	 
			}
		}
}
