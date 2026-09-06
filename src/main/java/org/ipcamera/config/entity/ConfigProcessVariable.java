package org.ipcamera.config.entity;

import lombok.Getter;
import lombok.Setter;

import static org.ipcamera.config.constants.Constants.GOOGLE_NTP_TIME;

@Getter
@Setter
public class ConfigProcessVariable {
    private int threadCount;
    private long intervalMinutes;
    private Boolean checkRTSP;
    private int delta;
    private String urlDBFirebird;
    private String ntpHost;
    private Boolean autoUpdateIPCamera;
    private Boolean autoUpdateAxis;
    private Boolean autoUpdateManual;
    private Boolean checkManual;
    private long intervalMinutesManual;
    private int deltaManual;

    public ConfigProcessVariable() {
        this.threadCount = 10;
        this.intervalMinutes = 30;
        this.checkRTSP = false;
        this.delta = 3;
        this.autoUpdateIPCamera = false;
        this.autoUpdateAxis = false;
        this.autoUpdateManual = false;
        this.intervalMinutesManual = 2;
        this.deltaManual = 3;
        this.checkManual = false;
        this.urlDBFirebird = "*";
        this.ntpHost = "*";
    }

    public ConfigProcessVariable(int threadCount, long intervalMinutes, Boolean checkRTSP, int delta
            , String urlDBFirebird, String ntpHost, Boolean autoUpdateIPCamera, Boolean autoUpdateAxis
            , Boolean autoUpdateManual, long intervalMinutesManual, int deltaManual, Boolean checkManual) {
        this.threadCount = threadCount;
        this.intervalMinutes = intervalMinutes;
        this.checkRTSP = checkRTSP;
        this.delta = delta;
        this.urlDBFirebird = urlDBFirebird;
        this.ntpHost = ntpHost;
        this.autoUpdateIPCamera = autoUpdateIPCamera;
        this.autoUpdateAxis = autoUpdateAxis;
        this.autoUpdateManual = autoUpdateManual;
        this.intervalMinutesManual = intervalMinutesManual;
        this.deltaManual = deltaManual;
        this.checkManual = checkManual;
    }
}
