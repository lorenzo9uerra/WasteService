package it.unibo;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import unibo.comm22.utils.ColorsOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WasteServiceTrolleyPosObserver implements CoapHandler {
    private List<String> history = new ArrayList<>();
    private SimplePayloadExtractor posExtract = new SimplePayloadExtractor("tpos");

    @Override
    public void onLoad(CoapResponse response) {
        String content = response.getResponseText();
        List<String> payload = posExtract.extractPayload(PrologUtils.getFuncLine(content, "tpos"));
        ColorsOut.outappl("Obs WSTrolley | tpos: " + payload.get(0), ColorsOut.GREEN);
        String newPos = payload.get(0);
        boolean add = history.size() == 0;
        if (!add) {
            String last = history.get(history.size() - 1);
            add = !last.equals(newPos);
        }
        if (add)
            history.add(newPos);
        ColorsOut.outappl("Obs WSTrolley | tpos history: " + history, ColorsOut.GREEN);
    }

    @Override
    public void onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)");
    }

    public List<String> getHistory() {
        return history;
    }
}
