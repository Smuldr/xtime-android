package com.xebia.xtime.test.editor;

import android.test.InstrumentationTestCase;

import com.xebia.xtime.editor.worktypesloader.WorkTypeListParser;
import com.xebia.xtime.shared.model.WorkType;
import com.xebia.xtime.test.Mocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkTypeListParserTest extends InstrumentationTestCase {

    public void testRegularInput() throws Exception {
        List<WorkType> expected = new ArrayList<>();
        expected.add(new WorkType("400", "Fixed Price Consultancy"));
        expected.add(new WorkType("420", "Consultancy on Maintenance contracts"));
        expected.add(new WorkType("430", "Free work/ cost for  Xebia"));
        expected.add(new WorkType("450", "Trainings (given)"));
        expected.add(new WorkType("455", "Trainings (preparation)"));

        String input = Mocks.getWorkTypesForProjectResponse(getInstrumentation().getContext());
        List<WorkType> result = WorkTypeListParser.parse(input);

        assertTrue(Arrays.equals(expected.toArray(), result.toArray()));
    }

    public void testUnparseableInput() throws Exception {
        List<WorkType> result = WorkTypeListParser.parse("this is intentionally not parseable");
        assertNull(result);
    }

    public void testEmptyInput() throws Exception {
        List<WorkType> result = WorkTypeListParser.parse("");
        assertNull(result);
    }

    public void testNullInput() throws Exception {
        List<WorkType> result = WorkTypeListParser.parse(null);
        assertNull(result);
    }
}
