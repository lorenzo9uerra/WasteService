package it.unibo.lenziguerra.wasteservice.wasteservice

import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import it.unibo.lenziguerra.wasteservice.SystemConfig
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import unibo.comm22.utils.CommUtils
import javax.annotation.PreDestroy
import kotlin.concurrent.thread

fun main(args: Array<String>) {
	runApplication<WasteserviceApplication>(*args)
}

@SpringBootApplication
class WasteserviceApplication {
	@Autowired(required = false)
	var ctxBean : WasteServiceContextBean? = null

	init {
		SystemConfig.setConfiguration("SystemConfig.json")
	}
}

@Profile("!noqak")
@Component
class WasteServiceContextBean {
	final val qakCtx: QakContext

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

		println("WasteServiceContextBean loaded!")
	}

	@PreDestroy
	fun preShutdown() {
		qakCtx.terminateTheContext()
	}
}