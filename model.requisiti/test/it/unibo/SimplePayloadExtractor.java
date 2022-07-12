package it.unibo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimplePayloadExtractor {
    private String id;

    public SimplePayloadExtractor(String id) {
        this.id = id;
    }

    public List<String> extractPayload(String str) {
        Pattern pattern = Pattern.compile(id + "\\(([^\\)]*)\\)");
        Matcher matcher = pattern.matcher(str);
        String payload = null;
        if (matcher.matches()) {
            payload = matcher.group(1);

            if (payload == null) {
                Matcher emptyMatcher = Pattern.compile(id + "\\(\\s*\\)").matcher(str);
                if (!emptyMatcher.matches()) {
                    throw new IllegalArgumentException("Message not properly formatted: expected " + id + "(X, Y...), got " + str);
                }
            }
        } else {
            throw new IllegalArgumentException("Message not properly formatted: expected " + id + "(X, Y...), got " + str);
        }

        List<String> out = Arrays.stream(payload.split("\\s*,\\s*")).collect(Collectors.toList());
        if (out.size() == 1 && out.get(0).equals("")) {
            return new ArrayList<String>();
        } else {
            return out;
        }
    }
}
