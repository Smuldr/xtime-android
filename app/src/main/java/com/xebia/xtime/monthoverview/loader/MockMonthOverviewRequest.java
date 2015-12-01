package com.xebia.xtime.monthoverview.loader;

import com.xebia.xtime.shared.XTimeAuthenticationException;
import com.xebia.xtime.shared.XTimeRequest;

public class MockMonthOverviewRequest extends XTimeRequest {

    private static final String RESULT = "throw 'allowScriptTagRemoting is false.';\n" +
            "//#DWR-INSERT\n" +
            "//#DWR-REPLY\n" +
            "s25={};var s26={};var s27={};var s28={};var s29={};var s30={};var s31={};var s32={};var s0=[];var s2={};var s9=[];var s10={};var s11={};var s12={};var s13={};var s14={};var s3={};var s15=[];var s16={};var s4={};var s17=[];var s18={};var s19={};var s20={};var s21={};var s5={};var s22=[];var s23={};var s6={};var s24=[];var var s7={};var s33=[];var s34={};var s35={};var s36={};var s37={};var s8={};var s38=[];var s39={};var s1=[];s0[0]=s2;s0[1]=s3;s0[2]=s4;s0[3]=s6;s0[4]=s7;s0[5]=s8;\n" +
            "s2.clientName=\"Widget Corp\";s2.description=\"\";s2.projectId=\"0940\";s2.projectName=\"Widget Corp Code\";s2.timeCells=s9;s2.userId=\"1015900\";s2.workTypeDescription=\"Consultancy Standard\";s2.workTypeId=\"100\";\n" +
            "s9[0]=s10;s9[1]=s11;s9[2]=s12;s9[3]=s13;s9[4]=s14;\n" +
            "s10.approved=false;s10.entryDate=new Date(1389567600000);s10.fromAfas=false;s10.hour=\"8.0\";s10.transferredToAfas=false;\n" +
            "s11.approved=false;s11.entryDate=new Date(1389740400000);s11.fromAfas=false;s11.hour=\"7.0\";s11.transferredToAfas=false;\n" +
            "s12.approved=false;s12.entryDate=new Date(1389826800000);s12.fromAfas=false;s12.hour=\"5.0\";s12.transferredToAfas=false;\n" +
            "s13.approved=false;s13.entryDate=new Date(1389654000000);s13.fromAfas=false;s13.hour=\"5.0\";s13.transferredToAfas=false;\n" +
            "s14.approved=false;s14.entryDate=new Date(1389913200000);s14.fromAfas=false;s14.hour=\"4.0\";s14.transferredToAfas=false;\n" +
            "s3.clientName=\"Acme Inc.\";s3.description=\"\";s3.projectId=\"1757\";s3.projectName=\"ACME Roadrunner Project\";s3.timeCells=s15;s3.userId=\"1015900\";s3.workTypeDescription=\"Consultancy Standard\";s3.workTypeId=\"100\";\n" +
            "s15[0]=s16;\n" +
            "s16.approved=true;s16.entryDate=new Date(1389049200000);s16.fromAfas=false;s16.hour=\"4.0\";s16.transferredToAfas=false;\n" +
            "s4.clientName=\"Bluth Company\";s4.description=\"\";s4.projectId=\"1\";s4.projectName=\"Bluth Company Banana Stand\";s4.timeCells=s17;s4.userId=\"1015900\";s4.workTypeDescription=\"Idle\";s4.workTypeId=\"930\";\n" +
            "s17[0]=s18;s17[1]=s19;s17[2]=s20;s17[3]=s21;\n" +
            "s18.approved=true;s18.entryDate=new Date(1389049200000);s18.fromAfas=false;s18.hour=\"1.0\";s18.transferredToAfas=false;\n" +
            "s19.approved=true;s19.entryDate=new Date(1389308400000);s19.fromAfas=false;s19.hour=\"2.0\";s19.transferredToAfas=false;\n" +
            "s20.approved=true;s20.entryDate=new Date(1389222000000);s20.fromAfas=false;s20.hour=\"1.0\";s20.transferredToAfas=false;\n" +
            "s21.approved=true;s21.entryDate=new Date(1389135600000);s21.fromAfas=false;s21.hour=\"1.0\";s21.transferredToAfas=false;\n" +
            "s6.clientName=\"Bluth Company\";s6.description=\"\";s6.projectId=\"1850\";s6.projectName=\"Bluth Company Development\";s6.timeCells=s24;s6.userId=\"1015900\";s6.workTypeDescription=\"Chicken Dance\";s6.workTypeId=\"400\";\n" +
            "s24[0]=s25;s24[1]=s26;s24[2]=s27;s24[3]=s28;s24[4]=s29;s24[5]=s30;s24[6]=s31;s24[7]=s32;\n" +
            "s25.approved=false;s25.entryDate=new Date(1389826800000);s25.fromAfas=false;s25.hour=\"3.0\";s25.transferredToAfas=false;\n" +
            "s26.approved=true;s26.entryDate=new Date(1388962800000);s26.fromAfas=false;s26.hour=\"8.0\";s26.transferredToAfas=false;\n" +
            "s27.approved=true;s27.entryDate=new Date(1389049200000);s27.fromAfas=false;s27.hour=\"3.0\";s27.transferredToAfas=false;\n" +
            "s28.approved=true;s28.entryDate=new Date(1389308400000);s28.fromAfas=false;s28.hour=\"3.0\";s28.transferredToAfas=false;\n" +
            "s29.approved=true;s29.entryDate=new Date(1389222000000);s29.fromAfas=false;s29.hour=\"7.0\";s29.transferredToAfas=false;\n" +
            "s30.approved=true;s30.entryDate=new Date(1389135600000);s30.fromAfas=false;s30.hour=\"7.0\";s30.transferredToAfas=false;\n" +
            "s31.approved=true;s31.entryDate=new Date(1388703600000);s31.fromAfas=false;s31.hour=\"6.0\";s31.transferredToAfas=false;\n" +
            "s32.approved=true;s32.entryDate=new Date(1388617200000);s32.fromAfas=false;s32.hour=\"8.0\";s32.transferredToAfas=false;\n" +
            "s7.clientName=\"Xebia Internal\";s7.description=\"XTime for Android\";s7.projectId=\"1\";s7.projectName=\"Internal Project\";s7.timeCells=s33;s7.userId=\"1015900\";s7.workTypeDescription=\"Internal work\";s7.workTypeId=\"940\";\n" +
            "s33[0]=s34;s33[1]=s35;s33[2]=s36;s33[3]=s37;\n" +
            "s34.approved=false;s34.entryDate=new Date(1389913200000);s34.fromAfas=false;s34.hour=\"4.0\";s34.transferredToAfas=false;\n" +
            "s35.approved=false;s35.entryDate=new Date(1389740400000);s35.fromAfas=false;s35.hour=\"1.0\";s35.transferredToAfas=false;\n" +
            "s36.approved=true;s36.entryDate=new Date(1388703600000);s36.fromAfas=false;s36.hour=\"2.0\";s36.transferredToAfas=false;\n" +
            "s37.approved=true;s37.entryDate=new Date(1389308400000);s37.fromAfas=false;s37.hour=\"3.0\";s37.transferredToAfas=false;\n" +
            "s8.clientName=\"Xebia Internal\";s8.description=\"\";s8.projectId=\"0062\";s8.projectName=\"XKE\";s8.timeCells=s38;s8.userId=\"1015900\";s8.workTypeDescription=\"XKE Participation\";s8.workTypeId=\"910\";\n" +
            "s38[0]=s39;\n" +
            "s39.approved=false;s39.entryDate=new Date(1389654000000);s39.fromAfas=false;s39.hour=\"3.0\";s39.transferredToAfas=false;\n" +
            "s1[0]=4;s1[1]=5;s1[2]=11;s1[3]=12;s1[4]=18;s1[5]=19;s1[6]=25;s1[7]=26;\n" +
            "dwr.engine._remoteHandleCallback('0','0',{lastTransferredDate:new Date(1388444400000),monthDaysCount:31,monthlyDataApproved:false,monthlyDataTransferred:false,projects:null,timesheetRows:s0,userName:\"Steven Mulder\",weekEndDates:s1,weekStart:null});\n";

    @Override
    public String submit() throws XTimeAuthenticationException {
        return RESULT;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE_PLAIN;
    }

    @Override
    public String getRequestData() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }
}
