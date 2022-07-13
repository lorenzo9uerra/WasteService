package it.unibo;

import it.unibo.ctxreq_deposit.MainCtxreq_depositKt;
import it.unibo.ctxreq_request.MainCtxreq_requestKt;
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

import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class TestRequest {
    final static String CTX_HOST = "localhost";
    final static int CTX_PORT = 8050;
    final static String CTX_TEST = "ctxreq_request";
    final static String ANON_ACTOR_ID = "test";

    String test_prefix = "req_";
    String actor_wasteservice = test_prefix + "wasteservice";

    @Before
    public void up() {
        CommSystemConfig.tracing = false;

        new Thread(TestRequestRunContextsKt::main).start();

        waitForWasteService();
    }

    @After
    public void down() {
        ColorsOut.outappl(this.getClass().getName() + " TEST END", ColorsOut.GREEN);
    }

    @Test
    public void testAccept() {
        String reply = askDeposit("glass", 10);
        ColorsOut.outappl("Reply: " + reply, ColorsOut.CYAN);
        assertTrue(reply.contains("loadaccept"));
    }

    @Test
    public void testDeny() {
        String reply = askDeposit("plastic", 999);
        ColorsOut.outappl("Reply: " + reply, ColorsOut.CYAN);
        assertTrue(reply.contains("loadrejected"));
    }

    protected String askDeposit(String type, int amount) {
        String truckRequest = MsgUtil.buildRequest(ANON_ACTOR_ID, "truckDeposit",
                "truckDeposit(" + type + ", " + amount + ")",
                actor_wasteservice
        ).toString();
        try {
            ConnTcp connTcp = new ConnTcp("localhost", CTX_PORT);
            return connTcp.request(truckRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void waitForWasteService() {
        ColorsOut.outappl(this.getClass().getName() + " waits for WasteService ... ", ColorsOut.GREEN);
        ActorBasic waitingActor = QakContext.Companion.getActor(actor_wasteservice);
        while (waitingActor == null) {
            CommUtils.delay(200);
            waitingActor = QakContext.Companion.getActor(actor_wasteservice);
        }
        ColorsOut.outappl("WasteService loaded", ColorsOut.GREEN);
    }
}
