package it.unibo.lenziguerra.wasteservice.wasteservice;

import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.IApplMessage;
import it.unibo.kactor.MsgUtil;
import it.unibo.kactor.QakContext;
import it.unibo.lenziguerra.wasteservice.ConnTcp;
import it.unibo.lenziguerra.wasteservice.RunCtxTestRequest;
import it.unibo.lenziguerra.wasteservice.SystemConfig;
import it.unibo.lenziguerra.wasteservice.utils.MsgUtilsWs;
import it.unibo.lenziguerra.wasteservice.utils.PrologUtils;
import it.unibo.lenziguerra.wasteservice.utils.WsConnSpring;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import unibo.comm22.coap.CoapConnection;
import unibo.comm22.interfaces.Interaction2021;
import unibo.comm22.utils.ColorsOut;
import unibo.comm22.utils.CommUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

// @SpringBootTest: crea applicazione Spring da classe trovata nel percorso
@SpringBootTest(classes = WasteserviceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringRunner.class)
public class TestRequest {
    private String ctx_storage;
    private Interaction2021 wsConn;

    @Autowired
    private WasteServiceController controller;
    @LocalServerPort
    private Integer port;

    @Before
    public void up() {
//        CommSystemConfig.tracing = false;
        SystemConfig.INSTANCE.setConfiguration("SystemConfig.json", false);
        SystemConfig.INSTANCE.getPositions().put("home", List.of(List.of(0, 0),List.of(0, 0)));
        SystemConfig.INSTANCE.getPositions().put("indoor", List.of(List.of(0, 1),List.of(1, 1)));
        SystemConfig.INSTANCE.getPositions().put("plastic_box", List.of(List.of(2, 1),List.of(2, 1)));
        SystemConfig.INSTANCE.getPositions().put("glass_box", List.of(List.of(3, 1),List.of(3, 1)));

        new Thread(() -> {
            new RunCtxTestRequest().main();
        }).start();

        waitForActors();

        // Verifica corretto avvio della webapp
        assertThat(controller).isNotNull();

        startWsConnection();

        ColorsOut.outappl("Starting TestRequest, port is " + port, ColorsOut.CYAN);
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
                SystemConfig.INSTANCE.getContexts().get("storage")
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

        String storageStatus = coapRequest("storage", ctx_storage, SystemConfig.INSTANCE.getContexts().get("storage"));
        List<String> storageLines = PrologUtils.INSTANCE.getFuncLines(storageStatus, "content");

        for (String line : storageLines) {
            List<String> payload = PrologUtils.INSTANCE.extractPayload(line);
            if (payload.get(0).equals(type)) {
                max = Float.parseFloat(payload.get(2));
                break;
            }
        }
        assertTrue("Max of " + type + " not correctly obtained for test", Math.abs(max) > 0.01);

        // Imposta storage a quasi pieno
        dispatch("storage", MsgUtil.buildDispatch(
                "test",
                "testStorageSet",
                String.format(
                        Locale.US,
                        "testStorageSet('\"%s\":%.0f')",
                        type,
                        max - amount + 5
                ),
                SystemConfig.INSTANCE.getContexts().get("storage")
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
                SystemConfig.INSTANCE.getContexts().get("storage")
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
            ColorsOut.outappl("Sending " + MsgUtilsWs.cleanMessage(msg), ColorsOut.CYAN);
            connTcp.forward(MsgUtilsWs.cleanMessage(msg));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected void waitForActors() {
        ColorsOut.outappl(this.getClass().getName() + " waits for actors ... ", ColorsOut.GREEN);

        ActorBasic storage = QakContext.Companion.getActor(SystemConfig.INSTANCE.getContexts().get("storage"));
        while (storage == null) {
            CommUtils.delay(200);
            storage = QakContext.Companion.getActor(SystemConfig.INSTANCE.getContexts().get("storage"));
        }

        ctx_storage = storage.getContext().getName();

        ColorsOut.outappl(String.format("Actors loaded, contexts are: %s", ctx_storage), ColorsOut.GREEN);
    }

    protected void startWsConnection() {
        wsConn = new WsConnSpring(String.format(
            "ws://%s:%d/truck",
            "localhost",
            port
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
