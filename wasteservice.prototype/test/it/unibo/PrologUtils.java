package it.unibo;

import java.util.Arrays;
import java.util.List;
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
}
