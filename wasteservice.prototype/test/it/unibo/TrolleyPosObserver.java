package it.unibo;

import kotlin.Pair;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import unibo.actor22comm.utils.ColorsOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TrolleyPosObserver implements CoapHandler {
    private List<int[]> history = new ArrayList<>();
    private SimplePayloadExtractor posExtract = new SimplePayloadExtractor("pos");

    @Override
    public void onLoad(CoapResponse response) {
        String content = response.getResponseText();
        List<String> payload = posExtract.extractPayload(PrologUtils.getFuncLine(content, "pos"));
        ColorsOut.outappl("Obs Trolley | pos: " + payload.get(0) + "," + payload.get(1), ColorsOut.GREEN);
        int[] newPos = new int[]{Integer.parseInt(payload.get(0)), Integer.parseInt(payload.get(1))};
        boolean add = history.size() == 0;
        if (!add) {
            int[] last = history.get(history.size() - 1);
            add = last[0] != newPos[0] || last[1] != newPos[1];
        }
        if (add)
            history.add(newPos);
        ColorsOut.outappl("Obs Trolley | pos history: " + history.stream().map(Arrays::toString).collect(Collectors.joining(",")), ColorsOut.GREEN);
    }

    @Override
    public void onError() {
        ColorsOut.outerr("OBSERVING FAILED (press enter to exit)");
    }

    public List<int[]> getHistory() {
        return history;
    }
}
