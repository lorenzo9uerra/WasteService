package it.unibo.lenziguerra.wasteservice.sonar

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.IApplMessage
import it.unibo.kactor.MsgUtil.buildEvent
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.utils.ConnTcp
import it.unibo.radarSystem22.domain.DeviceFactory
import it.unibo.radarSystem22.domain.interfaces.IDistance
import it.unibo.radarSystem22.domain.interfaces.IDistanceObservable
import it.unibo.radarSystem22.domain.interfaces.IDistanceObserver
import it.unibo.radarSystem22.domain.interfaces.ISonar
import it.unibo.radarSystem22.domain.utils.ColorsOut

class DistanceSimpleObserver : IDistanceObserver {
    override fun update(p0: IDistance?) {
        if (p0 != null) {
            if(p0.`val` < SystemConfig.DLIMIT)
                sonarEvent("sonarStop", "_")
            else
                sonarEvent("sonarResume", "_")
        }
    }

}


class SonarShim(name : String) : ActorBasic(name) {
    private var sonar : ISonar = DeviceFactory.createSonar()
    private var observableDistance: IDistanceObservable = DeviceFactory.makeDistanceObservable(sonar)
    private lateinit var observer : IDistanceObserver

    override suspend fun actorBody(msg: IApplMessage) {
        sonar.activate()
        observer = DistanceSimpleObserver()
        observableDistance.subscribe(observer)
    }

}

private fun sonarEvent(id: String, params: String) {
    val event = buildEvent(
        "test", id,
        "$id($params)"
    ).toString()
    try {
        val connTcp = ConnTcp("localhost", SystemConfig.contexts["sonar"])
        ColorsOut.outappl("Sending event: $id($params)", ColorsOut.CYAN)
        connTcp.forward(event)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
