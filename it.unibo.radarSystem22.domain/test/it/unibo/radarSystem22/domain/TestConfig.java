package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.utils.StaticConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class TestConfig {
    @Before
    public void before() {
        ConfigForTest.intField = 15;
        ConfigForTest.stringField = "hello";
    }

    @Test
    public void testNormalConfig() {
        int val1 = 5;
        String val2 = "hi";
        String cfg = "{'intField': " + val1 + ", 'stringField': '" + val2 + "'}";
        StringReader reader = new StringReader(cfg);
        StringWriter writer = new StringWriter();
        StaticConfig.setTheConfiguration(ConfigForTest.class, reader, writer);
        assertEquals(val1, ConfigForTest.intField);
        assertEquals(val2, ConfigForTest.stringField);
        assertEquals("", writer.toString());
    }

    @Test
    public void testIncompleteConfig() {
        int val1 = 5;
        String val2 = "hello";
        String cfg = "{'intField': " + val1 + "}";
        StringReader reader = new StringReader(cfg);
        StringWriter writer = new StringWriter();
        StaticConfig.setTheConfiguration(ConfigForTest.class, reader, writer);
        assertEquals(val1, ConfigForTest.intField);
        assertEquals(val2, ConfigForTest.stringField);
        String trimmedOut = writer.toString()
                .replace("\n", " ")
                .replace("    ", "")
                .replace("\"", "'");
        assertEquals("{ 'intField': " + val1 + ", 'stringField': '" + val2 + "' }", trimmedOut);
    }
}
