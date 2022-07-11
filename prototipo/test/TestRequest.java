import it.unibo.ctxwasteservice.MainCtxwasteserviceKt;
import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.QakContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import unibo.actor22comm.utils.ColorsOut;
import unibo.actor22comm.utils.CommUtils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestRequest {

    private static final String ACTOR_ID_WASTE_SERVICE = "requesthandler";
    private static final String ACTOR_ID_WASTE_TRUCK = "camion";

    @Before
    public void up() {
        new Thread(MainCtxwasteserviceKt::main).start();
        waitForWasteService();
    }

    protected void waitForWasteService() {
        ActorBasic wasteservice = QakContext.Companion.getActor(ACTOR_ID_WASTE_SERVICE);
        while (wasteservice == null) {
            ColorsOut.outappl("testLoadOk waits for appl ... ", ColorsOut.GREEN);
            CommUtils.delay(200);
            wasteservice = QakContext.Companion.getActor(ACTOR_ID_WASTE_SERVICE);
        }
    }

    @After
    public void down() {
        ColorsOut.outappl("testLoadOk ENDS", ColorsOut.BLUE);
    }

    @Test
    public void testDeny() {
        ColorsOut.outappl("testDeny STARTS", ColorsOut.BLUE);
        int amount = 200;
        String truckRequestStr = String.format(
                "msg(depositRequest,request,%s,%s,depositRequest(glass,%d),1)",
                ACTOR_ID_WASTE_TRUCK,
                ACTOR_ID_WASTE_SERVICE,
                amount
        );
        try {
            ConnTcp connTcp = new ConnTcp("localhost", 8050);
            String answer = connTcp.request(truckRequestStr);
            ColorsOut.outappl("answer=" + answer, ColorsOut.GREEN);
            connTcp.close();
            assertTrue(answer.contains("loadrejected"));
        } catch (Exception e) {
            ColorsOut.outerr("ERROR:" + e.getMessage());
            fail("Connection error: " + e.getMessage());
        }

        ColorsOut.outappl("\n\n\nSUCCESS\n\n\n", ColorsOut.YELLOW);
    }

    @Test
    public void testAccept() {
        ColorsOut.outappl("testLoadOk STARTS", ColorsOut.BLUE);
        int amount = 10;
        String truckRequestStr = String.format(
                "msg(depositRequest,request,%s,%s,depositRequest(glass,%d),1)",
                ACTOR_ID_WASTE_TRUCK,
                ACTOR_ID_WASTE_SERVICE,
                amount
        );
        try {
            ConnTcp connTcp = new ConnTcp("localhost", 8050);
            String answer = connTcp.request(truckRequestStr);
            ColorsOut.outappl("answer=" + answer, ColorsOut.GREEN);
            connTcp.close();
            assertTrue(answer.contains("loadaccept"));
            connTcp = new ConnTcp("localhost", 8050);
            answer = connTcp.receiveMsg();
            assertTrue(answer.contains("pickedUp"));
        } catch (Exception e) {
            ColorsOut.outerr("ERROR:" + e.getMessage());
        }
    }
}
