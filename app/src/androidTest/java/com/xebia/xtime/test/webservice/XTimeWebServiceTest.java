package com.xebia.xtime.test.webservice;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.test.Mocks;
import com.xebia.xtime.webservice.XTimeWebService;

import java.util.Date;

public class XTimeWebServiceTest extends InstrumentationTestCase {

    private MockWebServer mServer;
    private XTimeWebService mWebService;
    private Context mContext;

    public void setUp() throws Exception {
        super.setUp();
        mServer = new MockWebServer();
        mServer.play();
        mWebService = XTimeWebService.getInstance();
        mWebService.setBaseUrl(mServer.getUrl("/xtime/").toString());
        mContext = getInstrumentation().getContext();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        mServer.shutdown();
    }

    public void testLogin() throws Exception {
        final String cookie = "JSESSIONID=BEEF38177C0CA25A303951D5944A7A5B;"
                + " Path=/xtime/; Secure; HttpOnly";
        mServer.enqueue(new MockResponse()
                .setResponseCode(302)
                .setHeader("Set-Cookie", cookie)
                .setHeader("Location", mServer.getUrl("/xtime/entryform.html").toString()));
        mServer.enqueue(new MockResponse()
                .setResponseCode(200));

        String result = mWebService.login("foo", "bar");
        RecordedRequest request = mServer.takeRequest();

        assertEquals(cookie, result);
        assertEquals("j_username=foo&j_password=bar", request.getUtf8Body());
        assertEquals("application/x-www-form-urlencoded; charset=utf-8",
                request.getHeader("Content-Type"));
    }

    public void testGetMonthOverview() throws Exception {
        final String mockResponse = Mocks.getMonthOverviewResponse(mContext);
        mServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse));

        String result = mWebService.getMonthOverview(new Date(1388588275000l), "cookie");
        RecordedRequest request = mServer.takeRequest();

        assertEquals(mockResponse, result);
        assertEquals("callCount=1\n"
                + "page=/xtime/monthlyApprove.html\n"
                + "httpSessionId=\n"
                + "scriptSessionId=\n"
                + "c0-scriptName=TimeEntryServiceBean\n"
                + "c0-methodName=getMonthOverview\n"
                + "c0-id=0\n"
                + "c0-param0=string:2014-01-01\n"
                + "batchId=0", request.getUtf8Body());
        assertEquals("text/plain; charset=utf-8", request.getHeader("Content-Type"));
        assertEquals("cookie", request.getHeader("Cookie"));
    }

    public void testGetWeekOverview() throws Exception {
        final String mockResponse = Mocks.getWeekOverviewResponse(mContext);
        mServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse));

        String result = mWebService.getWeekOverview(new Date(1388588275000l), "cookie");
        RecordedRequest request = mServer.takeRequest();

        assertEquals(mockResponse, result);
        assertEquals("callCount=1\n"
                + "page=/xtime/entryform.html\n"
                + "httpSessionId=\n"
                + "scriptSessionId=\n"
                + "c0-scriptName=TimeEntryServiceBean\n"
                + "c0-methodName=getWeekOverview\n"
                + "c0-id=0\n"
                + "c0-param0=string:2014-01-01\n"
                + "c0-param1=boolean:true\n"
                + "batchId=0\n", request.getUtf8Body());
        assertEquals("text/plain; charset=utf-8", request.getHeader("Content-Type"));
        assertEquals("cookie", request.getHeader("Cookie"));
    }

    public void testGetWorkTypesForProject() throws Exception {
        final String mockResponse = Mocks.getWorkTypesForProjectResponse(mContext);
        mServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse));
        Project project = new Project("31415", "some project name");
        Date week = new Date(1426287600000l); // Sat, 14 Mar 2015, 0:00:00 CET
        String result = mWebService.getWorkTypesForProject(project, week, "cookie");
        RecordedRequest request = mServer.takeRequest();

        assertEquals(mockResponse, result);
        assertEquals("callCount=1\n"
                + "page=/xtime/entryform.html\n"
                + "httpSessionId=\n"
                + "scriptSessionId=\n"
                + "c0-scriptName=TimeEntryServiceBean\n"
                + "c0-methodName=getWorkTypesListForProjectInRange\n"
                + "c0-id=0\n"
                + "c0-param0=string:31415\n"
                + "c0-param1=string:2015-03-14\n"
                + "c0-param2=boolean:true\n"
                + "batchId=0", request.getUtf8Body());
        assertEquals("text/plain; charset=utf-8", request.getHeader("Content-Type"));
        assertEquals("cookie", request.getHeader("Cookie"));
    }

    public void testApproveMonth() throws Exception {
        mServer.enqueue(new MockResponse()
                .setResponseCode(302)
                .setHeader("Location",
                        mServer.getUrl("/xtime/monthlyApproveConfirmation.html").toString()));
        mServer.enqueue(new MockResponse()
                .setResponseCode(200));

        boolean result = mWebService.approveMonth(67, new Date(1388588275000l), "cookie");
        RecordedRequest request = mServer.takeRequest();

        assertTrue(result);
        assertEquals("approvalMonthYear=2014-01-01&approve=Approve&inp_grandTotal=67",
                request.getUtf8Body());
        assertEquals("application/x-www-form-urlencoded; charset=utf-8",
                request.getHeader("Content-Type"));
        assertEquals("cookie", request.getHeader("Cookie"));
    }
}
