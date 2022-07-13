package it.unibo;

import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.QakContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import unibo.actor22comm.coap.CoapConnection;
import unibo.actor22comm.utils.ColorsOut;
import unibo.actor22comm.utils.CommSystemConfig;
import unibo.actor22comm.utils.CommUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestIndoorMoreRequests {
    final static String CTX_HOST = "localhost";
    final static int CTX_PORT = 8050;
    final static String CTX_TEST = "ctxreq_indoor_more_requests";

    String test_prefix = "imr_";
    String actor_trolley = test_prefix + "trolley";

    TrolleyPosObserver trolleyPosObserver;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;

        new Thread(MainCtxReturnHomeKt::main).start();

        waitForTrolley();
        startTrolleyCoapConnection();
    }

    @After
    public void down() {
        ColorsOut.outappl(this.getClass().getName() + " TEST END", ColorsOut.GREEN);
    }

    @Test
    public void testNoMoreRequests() {
        // <verifica la mancanza di nuove richieste>

        int maxSecondsWait = 10;
        reachPosTest("home", maxSecondsWait);
    }

    @Test
    public void testMoreRequests() {
        // <verifica la presenza di nuove richieste>

        int maxSecondsWait = 10;
        reachPosTest("indoor", maxSecondsWait);
    }

    protected void reachPosTest(String pos, int maxSecondsWait) {
        for (int i = 0; i < maxSecondsWait; i++) {
            CommUtils.delay(1000);
            List<String> posHistory = trolleyPosObserver.getHistory();
            if (posHistory.get(posHistory.size() - 1).equals(pos)) {
                return;
            }
        }
        fail("Too much time to reach position " + pos + ", pos history was:<" + trolleyPosObserver.getHistory() + ">");
    }

    protected void waitForTrolley() {
        ColorsOut.outappl(this.getClass().getName() + " waits for trolley ... ", ColorsOut.GREEN);
        ActorBasic trolley = QakContext.Companion.getActor(actor_trolley);
        while (trolley == null) {
            CommUtils.delay(200);
            trolley = QakContext.Companion.getActor(actor_trolley);
        }
        ColorsOut.outappl("Trolley loaded", ColorsOut.GREEN);
    }

    protected void startTrolleyCoapConnection() {
        trolleyPosObserver = new TrolleyPosObserver();
        new Thread(() -> {
            CoapConnection conn = new CoapConnection(CTX_HOST + ":" + CTX_PORT, CTX_TEST + "/" + actor_trolley);
            conn.observeResource(trolleyPosObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }
}
