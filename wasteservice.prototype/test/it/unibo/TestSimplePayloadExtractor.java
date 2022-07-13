package it.unibo;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TestSimplePayloadExtractor {
    @Test
    public void test1() {
        String msg1 = "test()";
        String msg2 = "test(1)";
        String msg3 = "test(a, 2, 3)";
        String msgFail = "no()";

        SimplePayloadExtractor spe = new SimplePayloadExtractor("test");
        assertTrue(spe.extractPayload(msg1).isEmpty());
        assertAllEquals(List.of("1"), spe.extractPayload(msg2));
        assertAllEquals(List.of("a", "2", "3"), spe.extractPayload(msg3));
        try {
            spe.extractPayload(msgFail);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("Didn't fail in extracting wrong message");
    }

    private <T> void assertAllEquals(List<T> l1, List<T> l2) {
        try {
            assertEquals(l1.size(), l2.size());

            for (int i = 0; i < l1.size(); i++)
                assertEquals(l1.get(i), l2.get(i));
        } catch (AssertionError e) {
            throw new AssertionError("expected:<" + l1 + "> but was:<" + l2 + ">", e);
        }
    }
}
