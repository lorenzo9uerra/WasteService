package it.unibo;

import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.MsgUtil;
import it.unibo.kactor.QakContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import unibo.actor22comm.coap.CoapConnection;
import unibo.actor22comm.utils.ColorsOut;
import unibo.actor22comm.utils.CommSystemConfig;
import unibo.actor22comm.utils.CommUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestMoreRequests {
    final static String CTX_HOST = "localhost";
    final static int CTX_PORT = 8050;
    final static String CTX_TEST = "ctx_wasteservice_proto_ctx";

    String actor_trolley = "trolley";
    String actor_wasteservice = "wasteservice";

    TrolleyPosObserver trolleyPosObserver;
    WasteServiceTrolleyPosObserver wasteServiceObserver;

    int[] homeCoords;
    int[] indoorCoords;
    int[] glassBoxCoords;
    int[] plasticBoxCoords;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;

        new Thread(RunPrototypeNoTruck_Sprint1Kt::main).start();

        waitForTrolley();
        startWasteServiceCoapConnection();
    }

    @After
    public void down() {
        ColorsOut.outappl(this.getClass().getName() + " TEST END", ColorsOut.GREEN);
    }

    @Test
    public void testGoHome() {
        startDeposit("glass", 10);
        List<String> expectedPositions = List.of("home", "indoor", "glass_box", "home");

        int maxSecondsWait = 10;
        simplePositionsTest(expectedPositions, maxSecondsWait);
    }

    @Test
    public void testGoIndoor() {
        startDeposit("glass", 10);
        CommUtils.delay(500);
        startDeposit("glass", 10);
        List<String> expectedPositions = List.of("home", "indoor", "glass_box", "indoor");

        int maxSecondsWait = 10;
        simplePositionsTest(expectedPositions, maxSecondsWait);
    }

    protected void simplePositionsTest(List<String> expectedPositions, int maxSecondsWait) {
        for (int i = 0; i < maxSecondsWait; i++) {
            CommUtils.delay(1000);
            List<String> posHistory = wasteServiceObserver.getHistory();
            for (int j = 0; j < expectedPositions.size(); j++) {
                if (j >= posHistory.size()) {
                    break;
                } else {
                    assertEquals(expectedPositions.get(j), posHistory.get(j));
                }
            }

            // Se ogni posizione corrisponde, e si è raggiunta l'ultima, ritorna con successo
            if (expectedPositions.size() == posHistory.size()) {
                return;
            }
        }
        fail("Too much time to reach final position, pos history was:<" + trolleyPosObserver.getHistory() + ">");
    }

    protected void startDeposit(String type, int amount) {
        String startDepositDispatch = MsgUtil.buildRequest("wastetruck", "loadDeposit",
                "loadDeposit(" + type + ", " + amount + ")",
                actor_wasteservice
        ).toString();
        try {
            ConnTcp connTcp = new ConnTcp("localhost", CTX_PORT);
            connTcp.request(startDepositDispatch);
            ColorsOut.outappl("STARTED DEPOSIT VIA DISPATCH", ColorsOut.GREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    protected void startWasteServiceCoapConnection() {
        wasteServiceObserver = new WasteServiceTrolleyPosObserver();
        new Thread(() -> {
            CoapConnection conn = new CoapConnection(CTX_HOST + ":" + CTX_PORT, CTX_TEST + "/" + actor_wasteservice);
            conn.observeResource(wasteServiceObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }
}
