import it.unibo.kactor.*
import it.unibo.lenziguerra.wasteservice.BlinkLedState
import it.unibo.lenziguerra.wasteservice.ContextTestUtils
import it.unibo.lenziguerra.wasteservice.SystemConfig
import it.unibo.lenziguerra.wasteservice.data.LedStatus
import it.unibo.lenziguerra.wasteservice.sonar.SonarShim
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import unibo.comm22.utils.ColorsOut
import unibo.comm22.utils.CommSystemConfig
import unibo.comm22.utils.CommUtils
import unibo.comm22.coap.CoapConnection
import unibo.comm22.tcp.TcpClientSupport
import unibo.comm22.tcp.TcpConnection
import kotlin.concurrent.thread


class TestSonarShim {
    companion object {
        const val SONARS_ACTOR_NAME = "sonarshim"

        const val TEST_CONTEXT_NAME = "ctx_wasteservice_sonar_test"
        const val TEST_CONTEXT_HOST = "localhost"
        const val TEST_CONTEXT_PORT = 8050
        const val TEST_CONTEXT_DESC = """context($TEST_CONTEXT_NAME, "$TEST_CONTEXT_HOST",  "TCP", "$TEST_CONTEXT_PORT").
         qactor( $SONARS_ACTOR_NAME, $TEST_CONTEXT_NAME, "it.unibo.lenziguerra.wasteservice.sonar.SonarShim").
        """
    }

    lateinit var wasteserviceCtx: QakContext
    lateinit var sonarShim: SonarShim
    private lateinit var eventReceiver: TestEventActor

    @Before
    fun up() {
        CommSystemConfig.tracing = false

        thread { runBlocking {
            ContextTestUtils.createContextsFromString("localhost", this, TEST_CONTEXT_DESC, "sysRules.pl")
        } }

        waitForContexts()

        addTestComponents()
    }

    @Test
    fun sonarTest() {
        var first = true
        val values = listOf(50, 100, 150, 0)
        for (value in values) {
            if (first) {
                activateShim(value)
                first = false
            } else {
                forceSonarUpdate(value)
            }
            val event = eventReceiver.event
            val distance = PrologUtils.extractPayload(event.msgContent())[0].toInt()
            assertEquals(value, distance)
        }
    }

    private fun forceSonarUpdate(input: Int) {
        // Forza il sonar (opportunamente creato tramite mock)
        // a inviare un certo valore così che sonarshim lo riceva
    }

    private fun activateShim(firstInput: Int) {
        // Attiva SonarShim e inizia a osservare
        // la distanza del sonar, e invia come per forceSonarUpdate
        // il primo dato
    }

    private fun waitForContexts() {
        ColorsOut.outappl("WasteService loading...", ColorsOut.GREEN)
        waitForActor(SONARS_ACTOR_NAME)
        wasteserviceCtx = sysUtil.getContext(TEST_CONTEXT_NAME)!!
        sonarShim = QakContext.getActor(SONARS_ACTOR_NAME) as SonarShim
        ColorsOut.outappl("WasteService loaded", ColorsOut.GREEN)
    }

    private fun waitForActor(actor: String) {
        var waitingActor = QakContext.getActor(actor)
        while (waitingActor == null) {
            CommUtils.delay(200)
            waitingActor = QakContext.getActor(actor)
        }
    }

    private fun addTestComponents() {
        eventReceiver = TestEventActor("eventreceiver")

        wasteserviceCtx.addActor(eventReceiver)
        ColorsOut.outappl("WasteService: added fake actor <${eventReceiver.name}>", ColorsOut.GREEN)
    }

    internal class TestEventActor(name: String) : ActorBasic(name) {
        var lastEvent: IApplMessage? = null
        private val channel = Channel<IApplMessage>(1)
        val event: IApplMessage
            get() = runBlocking { channel.receive() }

        override suspend fun actorBody(msg: IApplMessage) {
            if (msg.isEvent()) {
                ColorsOut.outappl("TestEventActor $name: received event <$msg>", ColorsOut.GREEN)
                lastEvent = msg
                channel.send(msg)
            }
        }
    }
}