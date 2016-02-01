package com.xebia.xtime.webservice;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.xebia.xtime.BuildConfig;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.TimeSheetEntry;
import com.xebia.xtime.webservice.requestbuilder.ApproveRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.DeleteEntryRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.GetMonthOverviewRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.GetWeekOverviewRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.LoginRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.SaveEntryRequestBuilder;
import com.xebia.xtime.webservice.requestbuilder.WorkTypesForProjectRequestBuilder;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Logger;
import timber.log.Timber;

public class XTimeWebService {

    private static XTimeWebService instance;

    private final OkHttpClient httpClient;
    private Uri baseUri = Uri.parse("https://xtime.xebia.com/xtime");

    public static void init(@NonNull final CookieJar cookieJar) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.cookieJar(cookieJar);
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        clientBuilder.addNetworkInterceptor(new SessionTimeoutInterceptor());
        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new Logger() {
                @Override
                public void log(String message) {
                    Timber.v(message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            clientBuilder.addInterceptor(loggingInterceptor);
        }
        instance = new XTimeWebService(clientBuilder.build());
    }

    @NonNull
    public static XTimeWebService getInstance() {
        if (null == instance) {
            throw new RuntimeException("Instance is not initialized yet");
        }
        return instance;
    }

    private XTimeWebService(final OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setBaseUrl(final String baseUrl) {
        baseUri = Uri.parse(baseUrl);
    }

    /**
     * @param username XTime account username (without '@xebia.com' postfix)
     * @param password XTime account password
     * @return Cookie header value
     * @throws IOException
     */
    public Cookie login(final String username, final String password) throws IOException {
        Timber.d("Login");
        RequestBody body = new LoginRequestBuilder().username(username).password(password).build();
        Response response = doRequest(body, "j_spring_security_check");
        while (null != response.priorResponse()) {
            response = response.priorResponse();
        }
        if (response.header("Location").contains("error")) {
            return null;
        }
        for (String cookieHeader : response.headers("Set-Cookie")) {
            if (cookieHeader.startsWith("JSESSIONID")) {
                HttpUrl url = HttpUrl.parse(baseUri.toString());
                return Cookie.parse(url, cookieHeader);
            }
        }
        return null;
    }

    public String getMonthOverview(final Date month) throws IOException {
        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(month);
            Timber.d("Get overview for month %s", calendar.get(Calendar.MONTH));
        }
        RequestBody body = new GetMonthOverviewRequestBuilder().month(month).build();
        Response response = doRequest(body, "dwr/call/plaincall/" +
                "TimeEntryServiceBean.getMonthOverview.dwr");
        return response.isSuccessful() ? response.body().string() : null;
    }

    public String getWeekOverview(final Date week) throws IOException {
        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(week);
            Timber.d("Get overview for week %s", calendar.get(Calendar.WEEK_OF_YEAR));
        }
        RequestBody body = new GetWeekOverviewRequestBuilder().week(week).build();
        Response response = doRequest(body, "dwr/call/plaincall/" +
                "TimeEntryServiceBean.getWeekOverview.dwr");
        return response.isSuccessful() ? response.body().string() : null;
    }

    public boolean approveMonth(final double grandTotal, final Date month) throws IOException {
        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(month);
            Timber.d("Approve month %s", (calendar.get(Calendar.MONTH) + 1));
        }
        RequestBody body = new ApproveRequestBuilder().grandTotal(grandTotal).month(month).build();
        Response response = doRequest(body, "monthlyApprove.html");
        while (null != response.priorResponse()) {
            response = response.priorResponse();
        }
        String locationHeader = response.header("Location");
        return locationHeader != null && !locationHeader.contains("error");
    }

    public String getWorkTypesForProject(final Project project, final Date week) throws IOException {
        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(week);
            Timber.d("Get work types for %s in %s", project, (calendar.get(Calendar.WEEK_OF_YEAR)));
        }
        RequestBody body = new WorkTypesForProjectRequestBuilder().project(project).week(week)
                .build();
        Response response = doRequest(body, "dwr/call/plaincall/"
                + "TimeEntryServiceBean.getWorkTypesListForProjectInRange.dwr");
        return response.isSuccessful() ? response.body().string() : null;
    }

    public boolean saveEntry(final TimeSheetEntry timeEntry) throws IOException {
        Timber.d("Save entry %s", timeEntry);
        RequestBody body = new SaveEntryRequestBuilder()
                .timeSheetEntry(timeEntry)
                .build();
        Response response = doRequest(body, "entryform.html");
        while (null != response.priorResponse()) {
            response = response.priorResponse();
        }
        String locationHeader = response.header("Location");
        return locationHeader != null && !locationHeader.contains("error");
    }

    public String deleteEntry(final TimeSheetEntry timeEntry) throws IOException {
        if (BuildConfig.DEBUG) {
            Timber.d("Delete entry %s", timeEntry);
        }
        RequestBody body = new DeleteEntryRequestBuilder()
                .project(timeEntry.getProject())
                .workType(timeEntry.getWorkType())
                .description(timeEntry.getDescription())
                .entryDate(timeEntry.getTimeCell().getEntryDate()).build();
        Response response = doRequest(body, "dwr/call/plaincall/"
                + "TimeEntryServiceBean.deleteTimeSheetEntries.dwr");
        return response.isSuccessful() ? response.body().string() : null;
    }

    private Response doRequest(final RequestBody body, final String path) throws IOException {
        final Request request = new Request.Builder()
                .url(baseUri.buildUpon().appendEncodedPath(path).build().toString())
                .post(body)
                .build();
        return httpClient.newCall(request).execute();
    }
}
