package it.unibo.lenziguerra.wasteservice.utils

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter

class TestStaticConfig {
    @Before
    fun before() {
        ConfigForTest.intField = 15
        ConfigForTest.stringField = "hello"
    }

    @Test
    fun testNormalConfig() {
        val val1 = 5
        val val2 = "hi"
        val cfg = "{'intField': $val1, 'stringField': '$val2'}"
        val reader = StringReader(cfg)
        val writer = StringWriter()
        StaticConfig.setConfiguration(ConfigForTest::class, ConfigForTest, reader, writer)
        Assert.assertEquals(val1.toLong(), ConfigForTest.intField.toLong())
        Assert.assertEquals(val2, ConfigForTest.stringField)
        Assert.assertEquals("", writer.toString())
    }

    @Test
    fun testIncompleteConfig() {
        val val1 = 5
        val val2 = "hello"
        val cfg = "{'intField': $val1}"
        val reader = StringReader(cfg)
        val writer = StringWriter()
        StaticConfig.setConfiguration(ConfigForTest::class, ConfigForTest, reader, writer)
        Assert.assertEquals(val1.toLong(), ConfigForTest.intField.toLong())
        Assert.assertEquals(val2, ConfigForTest.stringField)
        val trimmedOut = writer.toString()
            .replace("\n", " ")
            .replace("    ", "")
            .replace("\"", "'")
        Assert.assertEquals("{ 'intField': $val1, 'stringField': '$val2' }", trimmedOut)
    }
}