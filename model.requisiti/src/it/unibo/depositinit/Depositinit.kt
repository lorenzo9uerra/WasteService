/* Generated by AN DISI Unibo */ 
package it.unibo.depositinit

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Depositinit ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "init"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
						delay(1000) 
					}
					 transition( edgeName="goto",targetState="start", cond=doswitch() )
				}	 
				state("start") { //this:State
					action { //it:State
						
									var Material = if (kotlin.random.Random.nextFloat() > 0.5) "glass" else "plastic"
									var Quantity = kotlin.random.Random.nextInt(10, 30)	
						forward("testDeposit", "testDeposit($Material,$Quantity)" ,"trolley_dep" ) 
					}
				}	 
			}
		}
}
