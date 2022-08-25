package it.unibo;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import unibo.comm22.utils.ColorsOut;

import java.util.ArrayList;
import java.util.List;

public class TrolleyPosObserver implements CoapHandler {
    private List<String> history = new ArrayList<>();
    private SimplePayloadExtractor posExtract = new SimplePayloadExtractor("pos");

    @Override
    public void onLoad(CoapResponse response) {
        String content = response.getResponseText();
        List<String> payload = posExtract.extractPayload(content);
        ColorsOut.outappl("Obs Trolley | pos: " + payload.get(0), ColorsOut.GREEN);
        history.add(payload.get(0));
        ColorsOut.outappl("Obs Trolley | pos history: " + history, ColorsOut.GREEN);
    }

    @Override
    public void onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)");
    }

    public List<String> getHistory() {
        return history;
    }
}
