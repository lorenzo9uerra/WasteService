package it.unibo.lenziguerra.wasteservice;

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
    final static String ACTOR_TROLLEY = "trolley";
    final static String ACTOR_WASTESERVICE = "wasteservice";

    private String ctx_wasteservice;
    private String ctx_trolley;

    TrolleyPosObserver trolleyPosObserver;
    WasteServiceTrolleyPosObserver wasteServiceObserver;

    int[] homeCoords;
    int[] indoorCoords;
    int[] glassBoxCoords;
    int[] plasticBoxCoords;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;
        SystemConfig.INSTANCE.setConfiguration("SystemConfig.json");

        new Thread(() -> {
            new RunCtxTestMoreRequests().main();
        }).start();

        waitForActors();
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
        String startDepositDispatch = MsgUtil.buildRequest("test", "triggerDeposit",
                "triggerDeposit(" + type + ", " + amount + ")",
                ACTOR_WASTESERVICE
        ).toString();
        try {
            ConnTcp connTcp = new ConnTcp(
                    SystemConfig.INSTANCE.getHosts().get("wasteServiceContext"),
                    SystemConfig.INSTANCE.getPorts().get("wasteServiceContext")
            );
            connTcp.request(startDepositDispatch);
            ColorsOut.outappl("STARTED DEPOSIT VIA DISPATCH", ColorsOut.GREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void waitForActors() {
        ColorsOut.outappl(this.getClass().getName() + " waits for actors ... ", ColorsOut.GREEN);
        ActorBasic trolley = QakContext.Companion.getActor(ACTOR_TROLLEY);
        while (trolley == null) {
            CommUtils.delay(200);
            trolley = QakContext.Companion.getActor(ACTOR_TROLLEY);
        }
        ActorBasic wasteservice = QakContext.Companion.getActor(ACTOR_WASTESERVICE);
        while (wasteservice == null) {
            CommUtils.delay(200);
            wasteservice = QakContext.Companion.getActor(ACTOR_WASTESERVICE);
        }

        ctx_trolley = trolley.getContext().getName();
        ctx_wasteservice = wasteservice.getContext().getName();

        ColorsOut.outappl(String.format("Actors loaded, contexts are: %s, %s", ctx_trolley, ctx_wasteservice), ColorsOut.GREEN);
    }


    protected void startWasteServiceCoapConnection() {
        wasteServiceObserver = new WasteServiceTrolleyPosObserver();
        new Thread(() -> {
            CoapConnection conn = new CoapConnection(SystemConfig.INSTANCE.getHosts().get("wasteServiceContext")
                    + ":" + SystemConfig.INSTANCE.getPorts().get("wasteServiceContext"),
                    ctx_wasteservice + "/" + ACTOR_WASTESERVICE
            );
            conn.observeResource(wasteServiceObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }
}
