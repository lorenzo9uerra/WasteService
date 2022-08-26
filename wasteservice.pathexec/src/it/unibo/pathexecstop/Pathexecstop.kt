/* Generated by AN DISI Unibo */ 
package it.unibo.pathexecstop

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Pathexecstop ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		val interruptedStateTransitions = mutableListOf<Transition>()
		 var CurMoveTodo = ""    //Upcase, since var to be used in guards
		   var StepTime    = "300"
		   var PathTodo    = ""
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						  CurMoveTodo = "" 
									StepTime = pathexecStopSupport.readStepTime() //stepTimeConfig.json
						updateResourceRep( "pathexecsteptime($StepTime)"  
						)
						println("pathexecstop ready. StepTime=$StepTime")
					}
					 transition(edgeName="t00",targetState="doThePath",cond=whenRequest("dopath"))
				}	 
				state("doThePath") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("dopath(PATH)"), Term.createTerm("dopath(PATH)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 PathTodo = payloadArg(0)  
								updateResourceRep( "pathexecdopath($PathTodo)"  
								)
								pathut.setPath( PathTodo  )
						}
						println("pathexecstop pathTodo = ${pathut.getPathTodo()}")
					}
					 transition( edgeName="goto",targetState="nextMove", cond=doswitch() )
				}	 
				state("nextMove") { //this:State
					action { //it:State
						 CurMoveTodo = pathut.nextMove()  
					}
					 transition( edgeName="goto",targetState="endWorkOk", cond=doswitchGuarded({ CurMoveTodo.length == 0  
					}) )
					transition( edgeName="goto",targetState="doMove", cond=doswitchGuarded({! ( CurMoveTodo.length == 0  
					) }) )
				}	 
				state("doMove") { //this:State
					action { //it:State
						delay(300) 
					}
					 transition( edgeName="goto",targetState="doMoveW", cond=doswitchGuarded({ CurMoveTodo == "w"  
					}) )
					transition( edgeName="goto",targetState="doMoveTurn", cond=doswitchGuarded({! ( CurMoveTodo == "w"  
					) }) )
				}	 
				state("doMoveTurn") { //this:State
					action { //it:State
						updateResourceRep( "pathexecdoturn($CurMoveTodo)"  
						)
						forward("cmd", "cmd($CurMoveTodo)" ,"basicrobot" ) 
						request("setAlarm", "setAlarm(300)" ,"timer" )  
					}
					 transition(edgeName="t01",targetState="nextMove",cond=whenReply("triggerAlarm"))
					interrupthandle(edgeName="t02",targetState="stopped",cond=whenDispatch("stopPath"),interruptedStateTransitions)
				}	 
				state("doMoveW") { //this:State
					action { //it:State
						updateResourceRep( "pathexecdostep($CurMoveTodo)"  
						)
						request("step", "step($StepTime)" ,"basicrobot" )  
					}
					 transition(edgeName="t03",targetState="endWorkKo",cond=whenEvent("alarm"))
					transition(edgeName="t04",targetState="nextMove",cond=whenReply("stepdone"))
					transition(edgeName="t05",targetState="endWorkKo",cond=whenReply("stepfail"))
					interrupthandle(edgeName="t06",targetState="stopped",cond=whenDispatch("stopPath"),interruptedStateTransitions)
				}	 
				state("endWorkOk") { //this:State
					action { //it:State
						println("endWorkOk: PATH DONE - BYE")
						updateResourceRep( "path $PathTodo done"  
						)
						answer("dopath", "dopathdone", "dopathdone(ok)"   )  
					}
					 transition( edgeName="goto",targetState="s0", cond=doswitch() )
				}	 
				state("endWorkKo") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						 var PathStillTodo = pathut.getPathTodo()  
						updateResourceRep( "pathstilltodo($PathStillTodo)"  
						)
						println("PATH FAILURE - SORRY. PathStillTodo=$PathStillTodo")
						answer("dopath", "dopathfail", "dopathfail($PathStillTodo)"   )  
					}
					 transition( edgeName="goto",targetState="s0", cond=doswitch() )
				}	 
				state("stopped") { //this:State
					action { //it:State
						 MsgUtil.outred("pathexecstop: stopped!")  
						updateResourceRep( pathexecstopped()  
						)
					}
					 transition(edgeName="t07",targetState="resume",cond=whenDispatch("resumePath"))
				}	 
				state("resume") { //this:State
					action { //it:State
						 MsgUtil.outgreen("pathexecstop: resumed!")  
						returnFromInterrupt(interruptedStateTransitions)
					}
				}	 
			}
		}
}
