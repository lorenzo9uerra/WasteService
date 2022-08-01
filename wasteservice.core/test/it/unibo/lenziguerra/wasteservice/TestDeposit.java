package it.unibo.lenziguerra.wasteservice;

import it.unibo.kactor.*;
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import unibo.actor22comm.coap.CoapConnection;
import unibo.actor22comm.utils.ColorsOut;
import unibo.actor22comm.utils.CommSystemConfig;
import unibo.actor22comm.utils.CommUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestDeposit {
    private String ctx_wasteservice;
    private String ctx_trolley;
    private String ctx_storage;

    TrolleyPosObserver trolleyPosObserver;
    WasteServiceTrolleyPosObserver wasteServiceObserver;

    Integer[][] homeCoords;
    Integer[][] indoorCoords;
    Integer[][] glassBoxCoords;
    Integer[][] plasticBoxCoords;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;

        SystemConfig.INSTANCE.setConfiguration("SystemConfig.json", false);
        SystemConfig.INSTANCE.getPositions().put("home", List.of(List.of(0, 0),List.of(0, 0)));
        SystemConfig.INSTANCE.getPositions().put("indoor", List.of(List.of(0, 3),List.of(1, 3)));
        SystemConfig.INSTANCE.getPositions().put("plastic_box", List.of(List.of(2, 0),List.of(3, 0)));
        SystemConfig.INSTANCE.getPositions().put("glass_box", List.of(List.of(3, 2),List.of(3, 3)));

        configCoords();

        new Thread(() -> {
            new RunCtxTestDeposit().main();
        }).start();

        waitForActors();
        startTrolleyCoapConnection();
        startWasteServiceCoapConnection();
    }

    @After
    public void down() {
        ColorsOut.outappl(this.getClass().getName() + " TEST END", ColorsOut.GREEN);
    }

    @Test
    public void testPositionsGlass() {
        startDeposit("glass", 10);
        List<String> expectedPositions = List.of("home", "indoor", "glass_box");
        List<Integer[][]> expectedPositionsCoords = List.of(homeCoords, indoorCoords, glassBoxCoords);

        int maxSecondsWait = 10;
        positionsTest(expectedPositions, expectedPositionsCoords, maxSecondsWait);
    }

    @Test
    public void testPositionsPlastic() {
        startDeposit("plastic", 10);
        List<String> expectedPositions = List.of("home", "indoor", "plastic_box");
        List<Integer[][]> expectedPositionsCoords = List.of(homeCoords, indoorCoords, plasticBoxCoords);

        int maxSecondsWait = 10;
        positionsTest(expectedPositions, expectedPositionsCoords, maxSecondsWait);
    }

    @Test
    public void testDeposit() {
        dispatch("storage", MsgUtil.buildDispatch(
            "test",
            "testStorageReset",
            "testStorageReset()",
                SystemConfig.INSTANCE.getCtxNames().get("storage")
        ));

        startDeposit("glass", 15);

        int maxSecondsWait = 10;
        for (int i = 0; i < maxSecondsWait; i++) {
            CommUtils.delay(1000);
            String lastPos = wasteServiceObserver.getHistory().get(wasteServiceObserver.getHistory().size() - 1);
            if (lastPos.equals("error")) {
                fail("Movement error! Pos history is " + wasteServiceObserver.getHistory());
            }
            else if (lastPos.equals("glass_box")) {
                // Lascia tempo di scaricare pesi nel caso la posizione
                // sia stata appena raggiunta
                CommUtils.delay(1000);

                List<String> storageReplyLines = PrologUtils.INSTANCE.getFuncLines(
                        coapRequest("storage", ctx_storage, SystemConfig.INSTANCE.getCtxNames().get("storage")),
                        "content"
                );
                for (String line : storageReplyLines) {
                    List<String> args =  PrologUtils.INSTANCE.extractPayload(line);
                    float value = Float.parseFloat(args.get(1));
                    switch (args.get(0)) {
                        case "glass": assertEquals(15f, value, 0.0001f); break;
                        case "plastic": assertEquals(0f, value, 0.0001f); break;
                    }
                }
                return;
            }
        }
        fail("Too much time to reach final position, pos history was:<" + trolleyPosObserver.getHistory() + ">");
    }


    protected void positionsTest(List<String> expectedPositions, List<Integer[][]>  expectedCoords, int maxSecondsWait) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < maxSecondsWait) {
            waitForObserverUpdate(1000);

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
            // se corrispondono anche le coordinate
            if (expectedPositions.size() == posHistory.size()) {
                // Controlla che passi dalle coordinate richieste (non SOLO le coordinate richieste)
                List<int[]> coordHistory = trolleyPosObserver.getHistory();
                int matchedCoords = 0;
                for (int[] coord : coordHistory) {
                    if (matchesOneCoordInRectangle(coord, expectedCoords.get(matchedCoords))) {
                        matchedCoords++;
                    }
                }
                if (matchedCoords == expectedCoords.size()) {
                    return;
                } else {
                    fail("Finished positions but coordinates didn't match: "
                            + coordHistory.stream().map(Arrays::toString).collect(Collectors.joining(","))
                    );
                }
            }
        }
        fail("Too much time to reach final position, pos history was:<" + trolleyPosObserver.getHistory() + ">");
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

    protected boolean matchesOneCoordInRectangle(int[] coord, Integer[][] checkCorners) {
        for (int i = checkCorners[0][0]; i <= checkCorners[1][0]; i++) {
            for (int j = checkCorners[0][1]; j <= checkCorners[1][1]; j++) {
                if (coord[0] == i && coord[1] == j)
                    return true;
            }
        }
        return false;
    }

    protected void startDeposit(String type, int amount) {
        String startDepositDispatch = MsgUtil.buildRequest("test", "triggerDeposit",
                "triggerDeposit(" + type + ", " + amount + ")",
                SystemConfig.INSTANCE.getCtxNames().get("wasteService")
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

    protected void dispatch(String hostname, IApplMessage msg) {
        try {
            ConnTcp connTcp = new ConnTcp(
                    SystemConfig.INSTANCE.getHosts().get(hostname),
                    SystemConfig.INSTANCE.getPorts().get(hostname)
            );
            connTcp.forward(msg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected void waitForActors() {
        ColorsOut.outappl(this.getClass().getName() + " waits for actors ... ", ColorsOut.GREEN);
        ActorBasic trolley = QakContext.Companion.getActor(SystemConfig.INSTANCE.getCtxNames().get("trolley"));
        while (trolley == null) {
            CommUtils.delay(200);
            trolley = QakContext.Companion.getActor(SystemConfig.INSTANCE.getCtxNames().get("trolley"));
        }
        ActorBasic wasteservice = QakContext.Companion.getActor(SystemConfig.INSTANCE.getCtxNames().get("wasteService"));
        while (wasteservice == null) {
            CommUtils.delay(200);
            wasteservice = QakContext.Companion.getActor(SystemConfig.INSTANCE.getCtxNames().get("wasteService"));
        }
        ActorBasic storage = QakContext.Companion.getActor(SystemConfig.INSTANCE.getCtxNames().get("storage"));
        while (storage == null) {
            CommUtils.delay(200);
            storage = QakContext.Companion.getActor(SystemConfig.INSTANCE.getCtxNames().get("storage"));
        }

        ctx_trolley = trolley.getContext().getName();
        ctx_wasteservice = wasteservice.getContext().getName();
        ctx_storage = storage.getContext().getName();

        ColorsOut.outappl(String.format("Actors loaded, contexts are: %s, %s, %s", ctx_trolley, ctx_wasteservice, ctx_storage), ColorsOut.GREEN);
    }

    protected void startTrolleyCoapConnection() {
        trolleyPosObserver = new TrolleyPosObserver();
        new Thread(() -> {
            CoapConnection conn = new CoapConnection(SystemConfig.INSTANCE.getHosts().get("trolley")
                    + ":" + SystemConfig.INSTANCE.getPorts().get("trolley"),
                    ctx_trolley + "/" + SystemConfig.INSTANCE.getCtxNames().get("trolley")
            );
            conn.observeResource(trolleyPosObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }

    protected void startWasteServiceCoapConnection() {
        wasteServiceObserver = new WasteServiceTrolleyPosObserver();
        new Thread(() -> {
            CoapConnection conn = new CoapConnection(SystemConfig.INSTANCE.getHosts().get("wasteServiceContext")
                    + ":" + SystemConfig.INSTANCE.getPorts().get("wasteServiceContext"),
                    ctx_wasteservice + "/" + SystemConfig.INSTANCE.getCtxNames().get("wasteService")
            );
            conn.observeResource(wasteServiceObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }

    protected String coapRequest(String hostname, String context, String actor){
        CoapConnection reqConn = new CoapConnection(
                SystemConfig.INSTANCE.getHosts().get(hostname) + ":"
                        + SystemConfig.INSTANCE.getPorts().get(hostname),
                context + "/" + actor
        );
        String answer = reqConn.request("");
        ColorsOut.outappl("coapRequest answer=" + answer, ColorsOut.CYAN);
        return answer;
    }

    private Integer[][] doubleListToArray(List<List<Integer>> list) {
        return list.stream().map(l -> l.toArray(new Integer[0]))
                .toArray(Integer[][]::new);
    }

    protected void configCoords() {
        // Carica da config in futuro
        Map<String, List<List<Integer>>> positions = SystemConfig.INSTANCE.getPositions();
        homeCoords = doubleListToArray(positions.get("home"));
        indoorCoords = doubleListToArray(positions.get("indoor"));
        plasticBoxCoords = doubleListToArray(positions.get("plastic_box"));
        glassBoxCoords = doubleListToArray(positions.get("glass_box"));
    }
}
