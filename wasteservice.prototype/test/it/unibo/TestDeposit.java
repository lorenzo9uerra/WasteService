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

import java.util.List;

import static org.junit.Assert.*;

public class TestDeposit {
    final static String CTX_HOST = "localhost";
    final static int CTX_PORT = 8050;
    final static String CTX_TEST = "ctx_wasteservice_proto_ctx";

    String actor_trolley = "trolley";
    String actor_storage = "storagemanager";

    TrolleyPosObserver trolleyPosObserver;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;

        new Thread(RunPrototypeNoTruck_Sprint1Kt::main).start();

        waitForTrolley();
        startTrolleyCoapConnection();
    }

    @After
    public void down() {
        ColorsOut.outappl(this.getClass().getName() + " TEST END", ColorsOut.GREEN);
    }

    @Test
    public void testPositionsGlass() {
        startDeposit("glass", 10);
        List<String> expectedPositions = List.of("home", "indoor", "glass_box");

        int maxSecondsWait = 10;
        positionsTest(expectedPositions, maxSecondsWait);
    }

    @Test
    public void testPositionsPlastic() {
        startDeposit("plastic", 10);
        List<String> expectedPositions = List.of("home", "indoor", "plastic_box");

        int maxSecondsWait = 10;
        positionsTest(expectedPositions, maxSecondsWait);
    }

    @Test
    public void testDeposit() {
        String wasteBoxResponse = coapRequest(actor_storage);
        String[] lines = wasteBoxResponse.split("\n");
        SimplePayloadExtractor glassSpe = new SimplePayloadExtractor("glass");
        SimplePayloadExtractor plasticSpe = new SimplePayloadExtractor("plastic");

        float numGlass   = Float.parseFloat(glassSpe.extractPayload(lines[0]).get(0));
        float numPlastic = Float.parseFloat(plasticSpe.extractPayload(lines[1]).get(0));
        assertEquals(0f, numGlass, 0.0001f);
        assertEquals(0f, numPlastic, 0.0001f);

        startDeposit("glass", 15);

        int maxSecondsWait = 10;
        for (int i = 0; i < maxSecondsWait; i++) {
            CommUtils.delay(1000);
            String lastPos = trolleyPosObserver.getHistory().get(trolleyPosObserver.getHistory().size() - 1);
            if (lastPos.equals("glass_box")) {
                // Lascia tempo di scaricare pesi nel caso la posizione
                // sia stata appena raggiunta
                CommUtils.delay(200);
                wasteBoxResponse = coapRequest(actor_storage);
                lines = wasteBoxResponse.split("\n");

                numGlass   = Float.parseFloat(glassSpe.extractPayload(lines[0]).get(0));
                numPlastic = Float.parseFloat(plasticSpe.extractPayload(lines[1]).get(0));
                assertEquals(15f, numGlass, 0.0001f);
                assertEquals(0f, numPlastic, 0.0001f);
                return;
            }
        }
        fail("Too much time to reach final position, pos history was:<" + trolleyPosObserver.getHistory() + ">");
    }

    protected void positionsTest(List<String> expectedPositions, int maxSecondsWait) {
        for (int i = 0; i < maxSecondsWait; i++) {
            CommUtils.delay(1000);
            List<String> posHistory = trolleyPosObserver.getHistory();
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
        String startDepositDispatch = MsgUtil.buildDispatch("wastetruck", "loadDeposit",
                "loadDeposit(" + type + ", " + amount + ")",
                actor_trolley
        ).toString();
        try {
            ConnTcp connTcp = new ConnTcp("localhost", CTX_PORT);
            connTcp.forward(startDepositDispatch);
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

    protected void startTrolleyCoapConnection() {
        trolleyPosObserver = new TrolleyPosObserver();
        new Thread(() -> {
            CoapConnection conn = new CoapConnection(CTX_HOST + ":" + CTX_PORT, CTX_TEST + "/" + actor_trolley);
            conn.observeResource(trolleyPosObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();
    }

    protected String coapRequest(String actor){
        CoapConnection reqConn = new CoapConnection(CTX_HOST + ":" + CTX_PORT, CTX_TEST + "/" + actor);
        String answer = reqConn.request("");
        ColorsOut.outappl("coapRequest answer=" + answer, ColorsOut.CYAN);
        return answer;
    }
}
