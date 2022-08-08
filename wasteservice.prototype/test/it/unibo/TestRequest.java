package it.unibo;

import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.MsgUtil;
import it.unibo.kactor.QakContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import unibo.comm22.coap.CoapConnection;
import unibo.comm22.utils.ColorsOut;
import unibo.comm22.utils.CommSystemConfig;
import unibo.comm22.utils.CommUtils;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class TestRequest {
    final static String CTX_HOST = "localhost";
    final static int CTX_PORT = 8050;
    final static String CTX_TEST = "ctx_wasteservice_proto_ctx";
    final static String ANON_ACTOR_ID = "test";

    String actor_wasteservice = "wasteservice";

    static Thread contextThread = null;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;

        if (contextThread == null) {
            contextThread = new Thread(RunPrototypeNoTruck_Sprint1Kt::main);
            contextThread.start();
        }

        waitForWasteService();
    }

    @After
    public void down() {
        ColorsOut.outappl(this.getClass().getName() + " TEST END", ColorsOut.GREEN);
    }

    @Test
    public void testAccept() {
        System.out.println("Start testAccept");
        String reply = askDeposit("glass", 10);
        ColorsOut.outappl("Reply: " + reply, ColorsOut.CYAN);
        assertTrue(reply.contains("loadaccept"));
    }

    @Test
    public void testDeny() {
        System.out.println("Start testDeny");
        String reply = askDeposit("plastic", 999);
        ColorsOut.outappl("Reply: " + reply, ColorsOut.CYAN);
        assertTrue(reply.contains("loadrejected"));
    }

    @Test
    public void testPickedUp() throws Exception {
        System.out.println("Start testPickedUp");
        String truckRequest = MsgUtil.buildRequest("wastetruck", "loadDeposit",
                "loadDeposit(glass, 1)",
                actor_wasteservice
        ).toString();
        ConnTcp connTcp = new ConnTcp("localhost", CTX_PORT);

        // Crea "finto" attore wastetruck

        String reply = connTcp.request(truckRequest);
        ColorsOut.outappl("Reply: " + reply, ColorsOut.CYAN);

        // Ascolta "come" attore wastetruck
        //String dispatch = connTcp.receiveMsg();
        //ColorsOut.outappl("Received: " + dispatch, ColorsOut.CYAN);
        //assertTrue(dispatch.contains("pickedUp");
    }

    protected String askDeposit(String type, int amount) {
        String truckRequest = MsgUtil.buildRequest(ANON_ACTOR_ID, "loadDeposit",
                "loadDeposit(" + type + ", " + amount + ")",
                actor_wasteservice
        ).toString();
        try {
            ConnTcp connTcp = new ConnTcp("localhost", CTX_PORT);
            String reply = connTcp.request(truckRequest);
            connTcp.close();
            return reply;
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
