package com.xebia.xtime.test;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Mocks {

    public static String getMonthOverviewResponse(final Context context) throws IOException {
        return readFromFile(context, "get_month_overview_response.txt");
    }

    public static String getWeekOverviewResponse(final Context context) throws IOException {
        return readFromFile(context, "get_week_overview_response.txt");
    }

    public static String getWorkTypesForProjectResponse(final Context context) throws IOException {
        return readFromFile(context, "get_work_types_for_project_response.txt");
    }

    private static String readFromFile(final Context context, final String fileName) throws
            IOException {
        String result = null;

        InputStream inputStream = null;
        try {
            final AssetManager assetMgr = context.getResources().getAssets();
            inputStream = assetMgr.open(fileName);

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString;
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString).append("\n");
            }

            result = stringBuilder.toString();
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }

        return result;
    }
}
