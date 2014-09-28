package com.xebia.xtime.test.editor;

import com.xebia.xtime.editor.delete.DeleteEntryResponseParser;

import junit.framework.TestCase;

public class DeleteEntryResponseParserTest extends TestCase {

    private static final String INPUT_REGULAR = "throw 'allowScriptTagRemoting is false.';\n" +
            "//#DWR-INSERT\n" +
            "//#DWR-REPLY\n" +
            "dwr.engine._remoteHandleCallback('0','0',1);";
    private static final String INPUT_IRREGULAR = "throw 'allowScriptTagRemoting is false.';\n" +
            "//#DWR-INSERT\n" +
            "//#DWR-REPLY\n" +
            "dwr.engine._remoteHandleCallback('0','0',0);";

    public void testRegularInput() {
        assertTrue(DeleteEntryResponseParser.parse(INPUT_REGULAR));
    }

    public void testIrregularInput() {
        assertFalse(DeleteEntryResponseParser.parse(INPUT_IRREGULAR));
    }

    public void testUnparseableInput() {
        assertFalse(DeleteEntryResponseParser.parse("this is intentionally not parseable"));
    }

    public void testEmptyInput() {
        assertFalse(DeleteEntryResponseParser.parse(""));
    }

    public void testNullInput() {
        assertFalse(DeleteEntryResponseParser.parse(null));
    }
}
