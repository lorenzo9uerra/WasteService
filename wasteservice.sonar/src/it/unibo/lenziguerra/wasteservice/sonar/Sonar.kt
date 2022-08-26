package it.unibo.lenziguerra.wasteservice.sonar

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.IApplMessage
import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.interfaces.*
import it.unibo.radarSystem22.domain.utils.ColorsOut
import kotlinx.coroutines.runBlocking

class SonarShim(name : String) : ActorBasic(name) {
    var sonar : ISonar = DeviceFactory.createSonar()
    var observableDistance: IDistanceObservable = DeviceFactory.makeDistanceObservable(sonar)
    var observableSonar = DeviceFactory.makeSonarObservable(sonar)
    lateinit var observer : IDistanceObserver

    override suspend fun actorBody(msg: IApplMessage) {
        if (msg.msgId() == "sonarStart") {
            observer = DistanceObserver()
            observableDistance.subscribe(observer)
            observableSonar.subscribe(object : ISonarObserver {
                override fun activated() {
                    ColorsOut.out("$name | Sonar was activated")
                }

                override fun deactivated() {
                    ColorsOut.out("$name | Sonar was deactivated")
                }
            })
            sonar.activate()
            ColorsOut.outappl("SonarShim initialized!", ColorsOut.ANSI_PURPLE)
        }
    }

    inner class DistanceObserver : IDistanceObserver {
        override fun update(distance: IDistance) {
            ColorsOut.out("$name | Received distance: $distance", ColorsOut.MAGENTA)
            runBlocking {
                emit("sonarDistance", "sonarDistance(${distance.`val`.toString()})")
                ColorsOut.out("$name | Emitted distance $distance", ColorsOut.BLUE)
            }
        }
    }
}