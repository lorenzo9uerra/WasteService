package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import it.unibo.lenziguerra.wasteservice.SystemConfig
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import unibo.actor22comm.utils.CommUtils
import javax.annotation.PreDestroy
import kotlin.concurrent.thread

fun main(args: Array<String>) {
	runApplication<WasteserviceApplication>(*args)
}

@SpringBootApplication
class WasteserviceApplication {
	@Autowired
	lateinit var ctxBean : WasteServiceContextBean

	init {
		SystemConfig.setConfiguration("SystemConfig.json")
	}
}

@Component
class WasteServiceContextBean {
	val qakCtx: QakContext

	init {
		thread {
			it.unibo.ctx_wasteservice.main()
		}

		var tmpQakCtx = sysUtil.getContext(SystemConfig.contexts["wasteServiceContext"]!!)
		while (tmpQakCtx == null) {
			CommUtils.delay(200)
			tmpQakCtx = sysUtil.getContext(SystemConfig.contexts["wasteServiceContext"]!!)
		}
		qakCtx = tmpQakCtx
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	@PreDestroy
	fun preShutdown() {
		qakCtx.terminateTheContext()
	}
}