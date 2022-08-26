/* Generated by AN DISI Unibo */ 
package it.unibo.sonarinterrupter

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Sonarinterrupter ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "idle"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		 
			// gira intorno a impossibilità di usare it.[...] dentro a stato
			// perchè it è già keyword
			val SystemConfig = it.unibo.lenziguerra.wasteservice.SystemConfig
			var prevDist: Float? = null
		return { //this:ActionBasciFsm
				state("idle") { //this:State
					action { //it:State
					}
					 transition(edgeName="t032",targetState="handleDistance",cond=whenEvent("sonarDistance"))
				}	 
				state("handleDistance") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("sonarDistance(DIST)"), Term.createTerm("sonarDistance(DIST)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 val dLimit = SystemConfig.DLIMIT  
								 val dist = payloadArg(0).toFloat()  
								if(  dist <= dLimit && prevDist?.let {it > dLimit } != false  
								 ){println("INVIO STOP")
								forward("trolleyStop", "trolleyStop(_)" ,"trolley" ) 
								}
								if(  dist > dLimit && prevDist?.let {it > dLimit } == false  
								 ){println("INVIO RESUME")
								forward("trolleyResume", "trolleyResume(_)" ,"trolley" ) 
								}
								 prevDist = dist  
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
			}
		}
}