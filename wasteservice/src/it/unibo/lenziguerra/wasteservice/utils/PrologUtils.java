package it.unibo.lenziguerra.wasteservice.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PrologUtils {
    public static List<String> getFuncLines(String lines, String func) {
        String[] linesArray = lines.split("\n");
        return Arrays.stream(linesArray).filter(l -> l.startsWith(func)).collect(Collectors.toList());
    }

    public static String getFuncLine(String lines, String func) {
        List<String> linesL = getFuncLines(lines, func);
        return (linesL.size() > 0) ? linesL.get(0) : null;
    }

    public static String extractId(String str) {
        String[] split = str.split("\\(");
        if (split.length > 0)
            return split[0];
        throw new IllegalArgumentException("Message not properly formatted: expected <word>(X, Y...), got " + str);
    }

    public static List<String> extractPayload(String str) {
        Pattern pattern = Pattern.compile("\\w+\\(([^\\)]*)\\)");
        Matcher matcher = pattern.matcher(str);
        String payload = null;
        if (matcher.matches()) {
            payload = matcher.group(1);

            if (payload == null) {
                Matcher emptyMatcher = Pattern.compile("\\w+\\(\\s*\\)").matcher(str);
                if (!emptyMatcher.matches()) {
                    throw new IllegalArgumentException("Message not properly formatted: expected <word>(X, Y...), got " + str);
                }
            }
        } else {
            throw new IllegalArgumentException("Message not properly formatted: expected <word>(X, Y...), got " + str);
        }

        List<String> out = Arrays.stream(payload.split("\\s*,\\s*")).collect(Collectors.toList());
        if (out.size() == 1 && out.get(0).equals("")) {
            return new ArrayList<String>();
        } else {
            return out;
        }
    }
}
