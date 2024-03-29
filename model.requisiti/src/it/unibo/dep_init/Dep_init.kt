/* Generated by AN DISI Unibo */ 
package it.unibo.dep_init

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Dep_init ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "wait"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		return { //this:ActionBasciFsm
				state("wait") { //this:State
					action { //it:State
						println("Attesa 2 secondi prima di inizio sistema per permettere test manuali...")
						delay(2000) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="send", cond=doswitch() )
				}	 
				state("send") { //this:State
					action { //it:State
						
									var Material = if (kotlin.random.Random.nextFloat() > 0.5) "glass" else "plastic"
									var Quantity = kotlin.random.Random.nextInt(10, 30)	
						forward("deposit", "deposit($Material,$Quantity)" ,"dep_trolley" ) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
				}	 
			}
		}
}
