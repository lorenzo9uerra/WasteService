import static org.junit.Assert.assertTrue;


import it.unibo.ctxwasteservice.MainCtxwasteserviceKt;
import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.QakContext;
import org.junit.*;
import unibo.actor22comm.utils.ColorsOut;
import unibo.actor22comm.utils.CommUtils;

public class TestDeposit {

    @Before
    public void up() {
        new Thread(MainCtxwasteserviceKt::main).start();
        waitForWasteService();
        waitForStorage();
    }

    protected void waitForWasteService() {
        ActorBasic wasteservice = QakContext.Companion.getActor("wasteservice");
        while (wasteservice == null) {
            ColorsOut.outappl("TestDeposit waits for wasteservice ... ", ColorsOut.GREEN);
            CommUtils.delay(200);
            wasteservice = QakContext.Companion.getActor("wasteservice");
        }
    }

    protected void waitForStorage() {
        ActorBasic wasteservice = QakContext.Companion.getActor("storage");
        while (wasteservice == null) {
            ColorsOut.outappl("TestDeposit waits for appl ... ", ColorsOut.GREEN);
            CommUtils.delay(200);
            wasteservice = QakContext.Companion.getActor("storage");
        }
    }

    @After
    public void down() {
        ColorsOut.outappl("testLoadOk ENDS", ColorsOut.BLUE);
    }

    @Test
    public void testLoadOk() {
        ColorsOut.outappl("testLoadOk STARTS", ColorsOut.BLUE);
        String truckRequestStr = "msg(depositRequest,request,wasteservice,trolley,depositRequest(glass,10),1)";
        try {
            ConnTcp connTcp = new ConnTcp("localhost", 8050);
            String answer = connTcp.request(truckRequestStr);
            ColorsOut.outappl("testLoadOk answer=" + answer, ColorsOut.GREEN);
            connTcp.close();
            assertTrue(answer.contains("doneDeposit"));
            
            truckRequestStr = "msg(storageAsk,request,wasteservice,storage,storageAsk(),1)";
            connTcp = new ConnTcp("localhost", 8050);
            answer = connTcp.request(truckRequestStr);
            ColorsOut.outappl("testLoadOk answer=" + answer, ColorsOut.GREEN);
            connTcp.close();
            assertTrue(answer.contains("10"));
        } catch (Exception e) {
            ColorsOut.outerr("testLoadOk ERROR:" + e.getMessage());

        }
    }
}
