import it.unibo.ctxwasteservice.MainCtxwasteserviceKt;
import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.QakContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import unibo.actor22comm.utils.ColorsOut;
import unibo.actor22comm.utils.CommUtils;

import static org.junit.Assert.assertTrue;

public class TestRequest {

    @Before
    public void up() {
        new Thread(MainCtxwasteserviceKt::main).start();
        waitForApplStarted();
    }

    protected void waitForApplStarted() {
        ActorBasic wasteservice = QakContext.Companion.getActor("wasteservice");
        while (wasteservice == null) {
            ColorsOut.outappl("testLoadOk waits for appl ... ", ColorsOut.GREEN);
            CommUtils.delay(200);
            wasteservice = QakContext.Companion.getActor("wasteservice");
        }
    }

    @After
    public void down() {
        ColorsOut.outappl("testLoadOk ENDS", ColorsOut.BLUE);
    }

    @Test
    public void testDeny() {
        ColorsOut.outappl("testDeny STARTS", ColorsOut.BLUE);
        String truckRequestStr = "msg(depositRequest,request,camion,wasteservice,depositRequest(glass,200),1)";
        try {
            ConnTcp connTcp = new ConnTcp("localhost", 8050);
            String answer = connTcp.request(truckRequestStr);
            ColorsOut.outappl("answer=" + answer, ColorsOut.GREEN);
            connTcp.close();
            assertTrue(answer.contains("loadrejected"));
        } catch (Exception e) {
            ColorsOut.outerr("ERROR:" + e.getMessage());

        }
    }

    @Test
    public void testAccept() {
        ColorsOut.outappl("testLoadOk STARTS", ColorsOut.BLUE);
        String truckRequestStr = "msg(depositRequest,request,camion,wasteservice,depositRequest(glass,10),1)";
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
