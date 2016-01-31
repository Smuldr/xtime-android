package com.xebia.xtime.shared.parser;

import android.text.TextUtils;

import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.TimeCell;
import com.xebia.xtime.shared.model.TimeSheetRow;
import com.xebia.xtime.shared.model.WorkType;
import com.xebia.xtime.shared.model.XTimeOverview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Parser for the response to a overview request from XTime. Uses regular expression acrobatics
 * to parse the JavaScript that is returned.
 */
public class XTimeOverviewParser {

    /**
     * Parses the input from a WeekOverviewRequest or MonthOverviewRequest into a {@link
     * com.xebia.xtime.shared.model.XTimeOverview}.
     *
     * @param input String with the JavaScript code that is returned to an overview request.
     * @return The week overview, or <code>null</code> when the input could not be parsed
     */
    public static XTimeOverview parse(String input) {
        if (TextUtils.isEmpty(input)) {
            Timber.d("No input to parse");
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
            boolean monthlyDataApproved = Boolean.parseBoolean(matcher.group(3));
            String username = matcher.group(5);
            List<Project> projects = parseProjects(input);
            List<TimeSheetRow> timeSheetRows = parseTimeSheetRows(input);
            return new XTimeOverview(timeSheetRows, projects, username, monthlyDataApproved,
                    new Date(lastTransferredDate));

        } else {
            Timber.d("Failed to parse input");
            return null;
        }
    }

    private static List<TimeSheetRow> parseTimeSheetRows(String input) {

        List<TimeSheetRow> timeSheetRows = new ArrayList<>();

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
            String description = matcher.group(3);
            String projectId = matcher.group(4);
            String projectName = matcher.group(5);
            List<TimeCell> timeCells = parseTimeCells(input, matcher.group(6));
            String workTypeDescription = matcher.group(8);
            if (description.equals(workTypeDescription)) {
                // XTime returns incorrect description for time sheet rows that come from Afas
                description = "";
            }
            String workTypeId = matcher.group(9);
            Project project = new Project(projectId, projectName);
            WorkType workType = new WorkType(workTypeId, workTypeDescription);

            timeSheetRows.add(new TimeSheetRow(project, workType, description, timeCells));
        }

        return timeSheetRows;
    }

    private static List<TimeCell> parseTimeCells(String input, String varName) {
        List<String> timeCellVarNames = parseTimeCellVars(input, varName);
        List<TimeCell> timeCells = new ArrayList<>();
        for (String timeCellVarName : timeCellVarNames) {
            timeCells.add(parseTimeCellDetails(input, timeCellVarName));
        }
        return timeCells;
    }

    private static TimeCell parseTimeCellDetails(String input, String varName) {

        // match the response for patterns like:
        // xx.approved=$1; xx.entryDate=new Date($2); xx.fromAfas=$3; x.hour="$4"; ...
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
            double hour = Double.parseDouble(matcher.group(4));
            return new TimeCell(new Date(entryDate), hour, approved);
        }

        return null;
    }

    private static List<String> parseTimeCellVars(String input, String varName) {
        List<String> varNames = new ArrayList<>();

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
        List<Project> projects = new ArrayList<>();

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
