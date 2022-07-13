package it.unibo;

import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.MsgUtil;
import it.unibo.kactor.QakContext;
import it.unibo.wasteservice.Wasteservice;
import kotlin.Pair;
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

public class TestDeposit {
    final static String CTX_HOST = "localhost";
    final static int CTX_PORT = 8050;
    final static String CTX_TEST = "ctx_wasteservice_proto_ctx";

    String actor_trolley = "trolley";
    String actor_wasteservice = "wasteservice";
    String actor_storage = "storagemanager";

    TrolleyPosObserver trolleyPosObserver;
    WasteServiceTrolleyPosObserver wasteServiceObserver;

    int[] homeCoords;
    int[] indoorCoords;
    int[] glassBoxCoords;
    int[] plasticBoxCoords;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;

        configCoords();

        new Thread(RunPrototypeNoTruck_Sprint1Kt::main).start();

        waitForTrolley();
        startTrolleyCoapConnection();
        startWasteServiceCoapConnection();
    }

    @After
    public void down() {
        ColorsOut.outappl(this.getClass().getName() + " TEST END", ColorsOut.GREEN);
    }

    @Test
    public void testTrolleyCollect() {
        trolleyRequest("trolleyCollect", "glass, 10");
        String trolleyContent = PrologUtils.getFuncLine(coapRequest(actor_trolley), "content");
        List<String> tContentParams = new SimplePayloadExtractor("content").extractPayload(trolleyContent);
        assertEquals("glass", tContentParams.get(0));
        assertEquals(10.0f, Float.parseFloat(tContentParams.get(1)), 0.0001f);
    }

    @Test
    public void testTrolleyDeposit() {
        trolleyRequest("trolleyCollect", "glass, 10");

        trolleyRequest("trolleyDeposit", "");

        assertNull(PrologUtils.getFuncLine(coapRequest(actor_trolley), "content"));

        List<String> storageContent = PrologUtils.getFuncLines(coapRequest(actor_storage), "content");
        for (String cnt : storageContent) {
            List<String> params = new SimplePayloadExtractor("content").extractPayload(cnt);
            switch (params.get(0)) {
                case "glass": assertEquals(10.0f, Float.parseFloat(params.get(1)), 0.0001f); break;
                case "plastic": assertEquals(0.0f, Float.parseFloat(params.get(1)), 0.0001f); break;
            }
        }
    }

    @Test
    public void testTrolleyMove() {
        trolleyRequest("trolleyMove", "3, 4");

        String posLine = PrologUtils.getFuncLine(coapRequest(actor_trolley), "pos");
        List<String> posParams = new SimplePayloadExtractor("pos").extractPayload(posLine);
        assertEquals(3, Float.parseFloat(posParams.get(0)), 0.001f);
        assertEquals(4, Float.parseFloat(posParams.get(1)), 0.001f);
    }

    @Test
    public void testPositionsGlass() {
        startDeposit("glass", 10);
        List<String> expectedPositions = List.of("home", "indoor", "glass_box");
        List<int[]> expectedPositionsCoords = List.of(homeCoords, indoorCoords, glassBoxCoords);

        int maxSecondsWait = 10;
        positionsTest(expectedPositions, expectedPositionsCoords, maxSecondsWait);
    }

    @Test
    public void testPositionsPlastic() {
        startDeposit("plastic", 10);
        List<String> expectedPositions = List.of("home", "indoor", "plastic_box");
        List<int[]> expectedPositionsCoords = List.of(homeCoords, indoorCoords, plasticBoxCoords);

        int maxSecondsWait = 10;
        positionsTest(expectedPositions, expectedPositionsCoords, maxSecondsWait);
    }

    @Test
    public void testDeposit() {
        String wasteBoxResponse = coapRequest(actor_storage);
        String[] lines = wasteBoxResponse.split("\n");
        SimplePayloadExtractor contentSpe = new SimplePayloadExtractor("content");

        float startNumGlass = 0, startNumPlastic = 0;
        List<String> storageReplyLines = PrologUtils.getFuncLines(coapRequest(actor_storage), "content");
        for (String line : storageReplyLines) {
            List<String> args =  contentSpe.extractPayload(line);
            switch (args.get(0)) {
                case "glass": startNumGlass = Float.parseFloat(args.get(1)); break;
                case "plastic": startNumPlastic = Float.parseFloat(args.get(1)); break;
            }
        }

        startDeposit("glass", 15);

        int maxSecondsWait = 10;
        for (int i = 0; i < maxSecondsWait; i++) {
            CommUtils.delay(1000);
            String lastPos = wasteServiceObserver.getHistory().get(wasteServiceObserver.getHistory().size() - 1);
            if (lastPos.equals("glass_box")) {
                // Lascia tempo di scaricare pesi nel caso la posizione
                // sia stata appena raggiunta
                CommUtils.delay(1000);

                float numGlass = 0, numPlastic = 0;
                storageReplyLines = PrologUtils.getFuncLines(coapRequest(actor_storage), "content");
                for (String line : storageReplyLines) {
                    List<String> args =  contentSpe.extractPayload(line);
                    switch (args.get(0)) {
                        case "glass": numGlass = Float.parseFloat(args.get(1)); break;
                        case "plastic": numPlastic = Float.parseFloat(args.get(1)); break;
                    }
                }
                assertEquals(15f, numGlass - startNumGlass, 0.0001f);
                assertEquals(0f, numPlastic - startNumPlastic, 0.0001f);
                return;
            }
        }
        fail("Too much time to reach final position, pos history was:<" + trolleyPosObserver.getHistory() + ">");
    }


    protected void positionsTest(List<String> expectedPositions, List<int[]> expectedCoords, int maxSecondsWait) {
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
            // se corrispondono anche le coordinate
            if (expectedPositions.size() == posHistory.size()) {
                // Controlla che passi dalle coordinate richieste (non SOLO le coordinate richieste)
                List<int[]> coordHistory = trolleyPosObserver.getHistory();
                int matchedCoords = 0;
                for (int[] coord : coordHistory) {
                    if (
                            coord[0] == expectedCoords.get(matchedCoords)[0]
                                    && coord[1] == expectedCoords.get(matchedCoords)[1]
                    ) {
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

    protected void trolleyRequest(String id, String params) {
        String request = MsgUtil.buildRequest("test", id,
                id + "(" + params + ")",
                actor_trolley
        ).toString();
        String reply = null;
        try {
            ConnTcp connTcp = new ConnTcp("localhost", CTX_PORT);
            ColorsOut.outappl("Asking trolley: " + id + "(" + params + ")", ColorsOut.CYAN);
            reply = connTcp.request(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (reply != null && reply.contains("false")) {
            fail("Trolley request <" + request + "> failed!");
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

    protected void startTrolleyCoapConnection() {
        trolleyPosObserver = new TrolleyPosObserver();
        new Thread(() -> {
            CoapConnection conn = new CoapConnection(CTX_HOST + ":" + CTX_PORT, CTX_TEST + "/" + actor_trolley);
            conn.observeResource(trolleyPosObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }

    protected void startWasteServiceCoapConnection() {
        wasteServiceObserver = new WasteServiceTrolleyPosObserver();
        new Thread(() -> {
            CoapConnection conn = new CoapConnection(CTX_HOST + ":" + CTX_PORT, CTX_TEST + "/" + actor_wasteservice);
            conn.observeResource(wasteServiceObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }

    protected String coapRequest(String actor){
        CoapConnection reqConn = new CoapConnection(CTX_HOST + ":" + CTX_PORT, CTX_TEST + "/" + actor);
        String answer = reqConn.request("");
        ColorsOut.outappl("coapRequest answer=" + answer, ColorsOut.CYAN);
        return answer;
    }

    protected void configCoords() {
        // Carica da config in futuro
        homeCoords = new int[]{0,0};
        indoorCoords = new int[]{0,5};
        plasticBoxCoords = new int[]{5,2};
        glassBoxCoords = new int[]{3,0};
    }
}
