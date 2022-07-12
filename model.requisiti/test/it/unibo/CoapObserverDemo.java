package it.unibo;

import it.unibo.ctxreq_deposit.MainCtxreq_depositKt;
import it.unibo.kactor.ActorBasic;
import it.unibo.kactor.MsgUtil;
import it.unibo.kactor.QakContext;
import unibo.actor22comm.coap.CoapConnection;
import unibo.actor22comm.utils.ColorsOut;
import unibo.actor22comm.utils.CommUtils;

public class CoapObserverDemo {
    public static final String CTX = "ctxreq_deposit";
    public static final String TROLLEY = "dep_trolley";
    public static final int PORT = 8050;

    public static void main(String[] args) {
        final TrolleyPosObserver trolleyObserver = new TrolleyPosObserver();

        new Thread(() -> MainCtxreq_depositKt.main()).start();

        ColorsOut.outappl("Waiting for Trolley...", ColorsOut.GREEN);
        ActorBasic trolley = QakContext.Companion.getActor(TROLLEY);
        while( trolley == null ){
            CommUtils.delay(200);
            trolley = QakContext.Companion.getActor(TROLLEY);
        }
        ColorsOut.outappl("Trolley loaded", ColorsOut.GREEN);

        new Thread(() -> {
            CoapConnection conn = new CoapConnection("localhost:" + PORT, CTX + "/" + TROLLEY);
            conn.observeResource(trolleyObserver);
            ColorsOut.outappl("connected via Coap conn:" + conn , ColorsOut.CYAN);
        }).start();

        String startDepositDispatch = MsgUtil.buildDispatch("_", "deposit", "deposit(glass, 10)", TROLLEY).toString();
        try {
            ConnTcp connTcp = new ConnTcp("localhost", 8050);
            connTcp.forward(startDepositDispatch);
            ColorsOut.outappl("STARTED DEPOSIT VIA DISPATCH", ColorsOut.GREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
