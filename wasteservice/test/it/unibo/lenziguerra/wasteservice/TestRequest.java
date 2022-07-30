package it.unibo.lenziguerra.wasteservice;

import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.IApplMessage;
import it.unibo.kactor.MsgUtil;
import it.unibo.kactor.QakContext;
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils;
import it.unibo.lenziguerra.wasteservice.wasteservice.WasteServiceController;
import it.unibo.lenziguerra.wasteservice.wasteservice.WasteServiceServerKt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import unibo.actor22comm.coap.CoapConnection;
import unibo.actor22comm.utils.ColorsOut;
import unibo.actor22comm.utils.CommSystemConfig;
import unibo.actor22comm.utils.CommUtils;
import unibo.actor22comm.ws.WsConnection;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

// @SpringBootTest: crea applicazione Spring da classe trovata nel percorso
@SpringBootTest
public class TestRequest {
    final static String ACTOR_STORAGE = "storagemanager";

    private String ctx_storage;
    private WsConnection wsConn;

    @Autowired
    private WasteServiceController controller;

    @Before
    public void up() {
        CommSystemConfig.tracing = false;
        SystemConfig.INSTANCE.setConfiguration("SystemConfig.json");

        new Thread(() -> {
            new RunCtxTestRequest().main();
        }).start();

        waitForActors();

        // Verifica corretto avvio della webapp
        assertThat(controller).isNotNull();

        startWsConnection();
    }

    @After
    public void down() {
        ColorsOut.outappl(this.getClass().getName() + " TEST END", ColorsOut.GREEN);
    }

    @Test
    public void testAccept() {
        System.out.println("Start testAccept");

        dispatch("storage", MsgUtil.buildDispatch(
                "test",
                "testStorageReset",
                "",
                ACTOR_STORAGE
        ));

        String reply = askDeposit("glass", 1);
        ColorsOut.outappl("Reply: " + reply, ColorsOut.CYAN);
        assertTrue(reply.contains("loadaccept"));
    }

    @Test
    public void testDeny() {
        System.out.println("Start testDeny");

        String type = "plastic";
        float max = 0;
        int amount = 10;

        String storageStatus = coapRequest("storage", ctx_storage, ACTOR_STORAGE);
        List<String> storageLines = PrologUtils.INSTANCE.getFuncLines("content", storageStatus);
        for (String line : storageLines) {
            List<String> payload = PrologUtils.INSTANCE.extractPayload(line);
            if (payload.get(0).equals(type)) {
                max = Float.parseFloat(payload.get(2));
                break;
            }
        }
        assertNotSame("Max of " + type + " not correctly obtained for test", .0f, max);

        // Imposta storage a quasi pieno
        dispatch("storage", MsgUtil.buildDispatch(
                "test",
                "testStorageSet",
                "{\"" + type + "\": " + (max - amount + 5) + "}",
                ACTOR_STORAGE
        ));

        String reply = askDeposit(type, amount);
        ColorsOut.outappl("Reply: " + reply, ColorsOut.CYAN);
        assertTrue(reply.contains("loadrejected"));
    }

    @Test
    public void testPickedUp() throws Exception {
        System.out.println("Start testPickedUp");

        dispatch("storage", MsgUtil.buildDispatch(
                "test",
                "testStorageReset",
                "",
                ACTOR_STORAGE
        ));

        String reply = askDeposit("glass", 1);
        ColorsOut.outappl("Reply: " + reply, ColorsOut.CYAN);

        String dispatch = wsConn.receiveMsg();
        ColorsOut.outappl("Received: " + dispatch, ColorsOut.CYAN);
        assertTrue(dispatch.contains("pickedUp"));
    }

    protected String askDeposit(String type, int amount) {
        try {
            return wsConn.request("loadDeposit(" + type + ", " + amount + ")");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return "";
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

        ActorBasic storage = QakContext.Companion.getActor(ACTOR_STORAGE);
        while (storage == null) {
            CommUtils.delay(200);
            storage = QakContext.Companion.getActor(ACTOR_STORAGE);
        }

        ctx_storage = storage.getContext().getName();

        ColorsOut.outappl(String.format("Actors loaded, contexts are: %s", ctx_storage), ColorsOut.GREEN);
    }

    protected void startWsConnection() {
        wsConn = new WsConnection(String.format(
            "ws://%s:%d/truck",
            SystemConfig.INSTANCE.getHosts().get("wasteServiceServer"),
            SystemConfig.INSTANCE.getPorts().get("wasteServiceServer")
        ));
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
}
