package com.xebia.xtime.test.editor;

import com.xebia.xtime.editor.worktypesloader.WorkTypeListParser;
import com.xebia.xtime.shared.model.WorkType;

import junit.framework.TestCase;

import java.util.List;

public class WorkTypeListParserTest extends TestCase {

    private static final String INPUT_UNPARSEABLE = "this is intentionally not parseable";
    private static final String INPUT_REGULAR = "throw 'allowScriptTagRemoting is false.';\n" +
            "//#DWR-INSERT\n" +
            "//#DWR-REPLY\n" +
            "var s0=[];var s1=[];var s2={};var s3={};var s4={};var s5={};var s6={};s0[0]=false;s0[1]=false;s0[2]=false;s0[3]=false;s0[4]=false;s0[5]=false;s0[6]=false;\n" +
            "s1[0]=s2;s1[1]=s3;s1[2]=s4;s1[3]=s5;s1[4]=s6;s1[5]=s7;s1[6]=s8;s1[7]=s9;s1[8]=s10;s1[9]=s11;s1[10]=s12;\n" +
            "s2.workType=\"400\";s2.workTypeDescription=\"Fixed Price Consultancy\";\n" +
            "s3.workType=\"420\";s3.workTypeDescription=\"Consultancy on Maintenance\";\n" +
            "s4.workType=\"430\";s4.workTypeDescription=\"Free work/ cost for  Xebia\";\n" +
            "s5.workType=\"450\";s5.workTypeDescription=\"Trainings (given)\";\n" +
            "s6.workType=\"455\";s6.workTypeDescription=\"Trainings (preparation)\";\n" +
            "s7.workType=\"500\";s7.workTypeDescription=\"Additional work type\";\n" +
            "s8.workType=\"520\";s8.workTypeDescription=\"This is also some type of work\";\n" +
            "s9.workType=\"530\";s9.workTypeDescription=\"Work work work\";\n" +
            "s10.workType=\"550\";s10.workTypeDescription=\"Typing\";\n" +
            "s11.workType=\"550\";s11.workTypeDescription=\"More typing\";\n" +
            "s12.workType=\"550\";s12.workTypeDescription=\"Thinking of more descriptions\";\n" +
            "dwr.engine._remoteHandleCallback('4','0',{dayEnabledStatus:s0,workTypeWorkGroupList:s1});";

    public void testRegularInput() {
        List<WorkType> result = WorkTypeListParser.parse(INPUT_REGULAR);

        // list of projects
        assertEquals(11, result.size());
        assertEquals(new WorkType("400", "Fixed Price Consultancy"), result.get(0));
        assertEquals(new WorkType("420", "Consultancy on Maintenance"), result.get(1));
        assertEquals(new WorkType("430", "Free work/ cost for  Xebia"), result.get(2));
    }

    public void testUnparseableInput() {
        List<WorkType> result = WorkTypeListParser.parse(INPUT_UNPARSEABLE);
        assertNull(result);
    }

    public void testEmptyInput() {
        List<WorkType> result = WorkTypeListParser.parse("");
        assertNull(result);
    }

    public void testNullInput() {
        List<WorkType> result = WorkTypeListParser.parse(null);
        assertNull(result);
    }
}