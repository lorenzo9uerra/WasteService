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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestMoreRequests {
    private String ctx_wasteservice;
    private String ctx_trolley;

    WasteServiceTrolleyPosObserver wasteServiceObserver;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;

        SystemConfig.INSTANCE.setConfiguration("SystemConfig.json", false);
        SystemConfig.INSTANCE.getPositions().put("home", List.of(List.of(0, 0),List.of(0, 0)));
        SystemConfig.INSTANCE.getPositions().put("indoor", List.of(List.of(0, 3),List.of(1, 3)));
        SystemConfig.INSTANCE.getPositions().put("plastic_box", List.of(List.of(2, 0),List.of(3, 0)));
        SystemConfig.INSTANCE.getPositions().put("glass_box", List.of(List.of(3, 2),List.of(3, 3)));

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
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < maxSecondsWait) {
            waitForObserverUpdate(1000);

            ColorsOut.outappl("Checking position...", ColorsOut.BLUE);

            List<String> posHistory = wasteServiceObserver.getHistory();
            if (posHistory.get(posHistory.size() - 1).equals("error")) {
                fail("Movement error! Pos history is " + posHistory);
            }
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
        fail("Too much time to reach final position, pos history was:<" + wasteServiceObserver.getHistory() + ">");
    }

    private void waitForObserverUpdate(int maxTimeMillis) {
        try {
            if (!wasteServiceObserver.getSemaphore().tryAcquire(1, maxTimeMillis, TimeUnit.MILLISECONDS)) {
                ColorsOut.outappl("Position check timeout", ColorsOut.BLUE);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void startDeposit(String type, int amount) {
        String startDepositDispatch = MsgUtil.buildRequest("test", "triggerDeposit",
                "triggerDeposit(" + type + ", " + amount + ")",
                SystemConfig.INSTANCE.getContexts().get("wasteService")
        ).toString();
        try {
            ConnTcp connTcp = new ConnTcp(
                    SystemConfig.INSTANCE.getHosts().get("wasteServiceContext"),
                    SystemConfig.INSTANCE.getPorts().get("wasteServiceContext")
            );
            connTcp.forward(startDepositDispatch);
            ColorsOut.outappl("STARTED DEPOSIT VIA DISPATCH", ColorsOut.GREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void waitForActors() {
        ColorsOut.outappl(this.getClass().getName() + " waits for actors ... ", ColorsOut.GREEN);
        ActorBasic trolley = QakContext.Companion.getActor(SystemConfig.INSTANCE.getContexts().get("trolley"));
        while (trolley == null) {
            CommUtils.delay(200);
            trolley = QakContext.Companion.getActor(SystemConfig.INSTANCE.getContexts().get("trolley"));
        }
        ActorBasic wasteservice = QakContext.Companion.getActor(SystemConfig.INSTANCE.getContexts().get("wasteService"));
        while (wasteservice == null) {
            CommUtils.delay(200);
            wasteservice = QakContext.Companion.getActor(SystemConfig.INSTANCE.getContexts().get("wasteService"));
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
                    ctx_wasteservice + "/" + SystemConfig.INSTANCE.getContexts().get("wasteService")
            );
            conn.observeResource(wasteServiceObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }
}
