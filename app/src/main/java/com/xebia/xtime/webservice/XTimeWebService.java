package com.xebia.xtime.webservice;

import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.xebia.xtime.BuildConfig;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.TimeSheetEntry;
import com.xebia.xtime.webservice.requestbuilder.ApproveRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.DeleteEntryRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.GetMonthOverviewRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.GetWeekOverviewRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.LoginRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.WorkTypesForProjectRequestBuilder;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class XTimeWebService {

    private static final XTimeWebService INSTANCE = new XTimeWebService();
    private final OkHttpClient mHttpClient;
    private Uri mBaseUri = Uri.parse("https://xtime.xebia.com/xtime");

    private XTimeWebService() {
        mHttpClient = new OkHttpClient();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        mHttpClient.setCookieHandler(cookieManager);
    }

    public static XTimeWebService getInstance() {
        return INSTANCE;
    }

    public void setBaseUrl(final String baseUrl) {
        mBaseUri = Uri.parse(baseUrl);
    }

    /**
     * @param username XTime account username (without '@xebia.com' postfix)
     * @param password XTime account password
     * @return Cookie header value
     * @throws IOException
     */
    public String login(final String username, final String password) throws IOException {
        Timber.d("Login");
        RequestBody body = new LoginRequestBuilder().username(username).password(password).build();
        Response response = doRequest(body, "j_spring_security_check");
        while (null != response.priorResponse()) {
            response = response.priorResponse();
        }
        return response.header("Set-Cookie");
    }

    public String getMonthOverview(final Date month, final String cookie) throws IOException {
        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(month);
            Timber.d("Get overview for month %s", calendar.get(Calendar.MONTH));
        }
        RequestBody body = new GetMonthOverviewRequestBuilder().month(month).build();
        Response response = doRequest(body, "dwr/call/plaincall/" +
                "TimeEntryServiceBean.getMonthOverview.dwr", cookie);
        return response.isSuccessful() ? response.body().string() : null;
    }

    public String getWeekOverview(final Date week, final String cookie) throws IOException {
        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(week);
            Timber.d("Get overview for week %s", calendar.get(Calendar.WEEK_OF_YEAR));
        }
        RequestBody body = new GetWeekOverviewRequestBuilder().week(week).build();
        Response response = doRequest(body, "dwr/call/plaincall/" +
                "TimeEntryServiceBean.getWeekOverview.dwr", cookie);
        return response.isSuccessful() ? response.body().string() : null;
    }

    public boolean approveMonth(final double grandTotal, final Date month,
                                final String cookie) throws IOException {
        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(month);
            Timber.d("Approve month %s", (calendar.get(Calendar.MONTH) + 1));
        }
        RequestBody body = new ApproveRequestBuilder().grandTotal(grandTotal).month(month).build();
        Response response = doRequest(body, "monthlyApprove.html", cookie);
        while (null != response.priorResponse()) {
            response = response.priorResponse();
        }
        String locationHeader = response.header("Location");
        return locationHeader != null && !locationHeader.contains("error");
    }

    public String getWorkTypesForProject(final Project project, final Date week,
                                         final String cookie) throws IOException {
        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(week);
            Timber.d("Get work types for %s in %s", project, (calendar.get(Calendar.WEEK_OF_YEAR)));
        }
        RequestBody body = new WorkTypesForProjectRequestBuilder().project(project).week(week)
                .build();
        Response response = doRequest(body, "dwr/call/plaincall/"
                + "TimeEntryServiceBean.getWorkTypesListForProjectInRange.dwr", cookie);
        return response.isSuccessful() ? response.body().string() : null;
    }

    public String deleteEntry(final TimeSheetEntry timeEntry, final String cookie) throws IOException {
        if (BuildConfig.DEBUG) {
            Timber.d("Delete entry %s", timeEntry);
        }
        RequestBody body = new DeleteEntryRequestBuilder()
                .project(timeEntry.getProject())
                .workType(timeEntry.getWorkType())
                .description(timeEntry.getDescription())
                .entryDate(timeEntry.getTimeCell().getEntryDate()).build();
        Response response = doRequest(body, "dwr/call/plaincall/"
                + "TimeEntryServiceBean.deleteTimeSheetEntries.dwr", cookie);
        return response.isSuccessful() ? response.body().string() : null;
    }

    private Response doRequest(final RequestBody body, final String path) throws IOException {
        return doRequest(body, path, "");
    }

    private Response doRequest(final RequestBody body, final String path,
                               final String cookie) throws IOException {
        Request request = new Request.Builder()
                .url(mBaseUri.buildUpon().appendEncodedPath(path).build().toString())
                .post(body)
                .header("Cookie", cookie != null ? cookie : "")
                .build();
        return mHttpClient.newCall(request).execute();
    }
}
