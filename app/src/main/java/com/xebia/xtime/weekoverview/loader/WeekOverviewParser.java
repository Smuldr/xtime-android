package com.xebia.xtime.weekoverview.loader;

import android.text.TextUtils;
import android.util.Log;

import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.TimeCell;
import com.xebia.xtime.shared.model.TimeSheetRow;
import com.xebia.xtime.shared.model.WeekOverview;
import com.xebia.xtime.shared.model.WorkType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the response to a {@link WeekOverviewRequest}. Uses regular expression acrobatics
 * to parse the JavaScript that is returned.
 */
public class WeekOverviewParser {

    private static final String TAG = "WeekOverviewParser";

    /**
     * Parses the input from a {@link WeekOverviewRequest} into a {@link WeekOverview}.
     *
     * @param input String with the JavaScript code that is returned to a WeekOverviewRequest.
     * @return The week overview, or <code>null</code> when the input could not be parsed
     */
    public static WeekOverview parse(String input) {
        if (TextUtils.isEmpty(input)) {
            Log.d(TAG, "No input to parse");
            return null;
        }

        // parse the JSONP callback argument
        String regex = "dwr\\.engine\\._remoteHandleCallback\\(";
        regex += ".*lastTransferredDate:new Date\\(([^\\)]*)\\)";
        regex += ",.*monthDaysCount:(\\d*)";
        regex += ",.*monthlyDataApproved:(\\w*)";
        regex += ",.*monthlyDataTransferred:(\\w*)";
        regex += ",.*userName:\"?([\\w\\s]*)\"?";
        regex += ",.*weekEndDates:(\\w*)";
        regex += ",.*weekStart:(\\w*)\\}\\);";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            // not all data that is returned is actually used in the app

            long lastTransferredDate = Long.parseLong(matcher.group(1));
            // int monthDaysCount = Integer.parseInt(matcher.group(2));
            boolean monthlyDataApproved = Boolean.parseBoolean(matcher.group(3));
            // boolean monthlyDataTransferred = Boolean.parseBoolean(matcher.group(4));
            List<Project> projects = parseProjects(input);
            List<TimeSheetRow> timeSheetRows = parseTimeSheetRows(input);
            String username = matcher.group(5);
            // String weekendDatesVar = matcher.group(6);
            // String weekStart = matcher.group(7);
            return new WeekOverview(timeSheetRows, projects, username, monthlyDataApproved, new Date(lastTransferredDate));

        } else {
            Log.d(TAG, "Failed to parse input '" + input + "'");
            return null;
        }
    }

    private static List<TimeSheetRow> parseTimeSheetRows(String input) {

        List<TimeSheetRow> timeSheetRows = new ArrayList<TimeSheetRow>();

        // match the response for patterns like:
        // xx.clientName="$1"; xx.description="$2"; xx.projectId="$3"; ...
        String regex = "(\\w*)\\.clientName=\"([^\"]*)\";" +
                ".*\\1\\.description=\"([^\"]*)\";" +
                ".*\\1\\.projectId=\"([^\"]*)\";" +
                ".*\\1\\.projectName=\"([^\"]*)\";" +
                ".*\\1\\.timeCells=([^;]*);" +
                ".*\\1\\.userId=\"([^\"]*)\";" +
                ".*\\1\\.workTypeDescription=\"([^\"]*)\";" +
                ".*\\1\\.workTypeId=\"([^\"]*)\";";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            // not all data that is returned is actually used in the app

            // String clientName = matcher.group(2);
            String description = matcher.group(3);
            String projectId = matcher.group(4);
            String projectName = matcher.group(5);
            List<TimeCell> timeCells = parseTimeCells(input, matcher.group(6));
            // String userId = matcher.group(7);
            String workTypeDescription = matcher.group(8);
            String workTypeId = matcher.group(9);
            Project project = new Project(projectId, projectName);
            WorkType workType = new WorkType(workTypeId, workTypeDescription);
            timeSheetRows.add(new TimeSheetRow(project, workType, description, timeCells));
        }

        return timeSheetRows;
    }

    private static List<TimeCell> parseTimeCells(String input, String varName) {

        List<String> timeCellVarNames = parseTimeCellVars(input, varName);

        List<TimeCell> timeCells = new ArrayList<TimeCell>();
        for (String timeCellVarName : timeCellVarNames) {
            timeCells.add(parseTimeCellDetails(input, timeCellVarName));
        }

        return timeCells;
    }

    private static TimeCell parseTimeCellDetails(String input, String varName) {

        // match the response for patterns like:
        // xx.approved=$1; xx.entryDate=new Date($2); xx.fromtAfas=$3; x.hour="$4"; ...
        String regex = varName + "\\.approved=([^;]*);";
        regex += ".*" + varName + "\\.entryDate=new Date\\(([^\\)]*)\\);";
        regex += ".*" + varName + "\\.fromAfas=([^;]*);";
        regex += ".*" + varName + "\\.hour=\"([^\"]*)\";";
        regex += ".*" + varName + "\\.transferredToAfas=([^;]*);";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            // not all data that is returned is actually used in the app

            boolean approved = "true".equals(matcher.group(1));
            long entryDate = Long.parseLong(matcher.group(2));
            // boolean fromAfas = "true".equals(matcher.group(3));
            double hour = Double.parseDouble(matcher.group(4));
            // boolean transferredToAfas = "true".equals(matcher.group(5));
            return new TimeCell(new Date(entryDate), hour, approved);
        }

        return null;
    }

    private static List<String> parseTimeCellVars(String input, String varName) {
        List<String> varNames = new ArrayList<String>();

        // match the response for patterns like:
        // xx[0]=$1; xx[1]=$2; ...
        String regex = varName + "\\[\\d*\\]=([^;,]*);";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            if (matcher.groupCount() == 1) {
                varNames.add(matcher.group(1));
            }
        }

        return varNames;
    }

    private static List<Project> parseProjects(String input) {
        List<Project> projects = new ArrayList<Project>();

        // match the response for patterns like:
        // xx.description="$1"; xx.id="$2";
        String regex = "(\\w*)\\.description=\"([^\"]*)\";" +
                ".*\\1\\.id=\"([^\"]*)\";";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String description = matcher.group(2);
            String id = matcher.group(3);
            projects.add(new Project(id, description));
        }

        return projects;
    }
}
