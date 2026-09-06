package org.ipcamera.config.entity;

import lombok.Getter;
import lombok.Setter;
import org.ipcamera.config.service.ExtractFromURLService;

import java.util.Date;

import static org.ipcamera.config.constants.ConstantsType.TYPE_NONE;

@Getter
@Setter
public class Camera {
    private String username;
    private String password;
    private String url;
    private String ipAddress;
    private int port;
    private String type;
    private String[] line;
    private String[] nickname;
    private String[] regname;
    private String[] transmitUrl;
    private String[] transmitUsr;
    private String[] transmitPsw;
    private Boolean manualMode = false;
    private Boolean banOnAutoUpdate = false;

    private Boolean statusPing = false;
    private Boolean statusRtsp = false;
    private long differenceTime = -1;
    private String version;
    private Date dateCheck;
    private Date dateUpdateSuccess;
    private Boolean statusGetRequest = false;

    public Camera(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.type = TYPE_NONE;
    }

    public Camera(String username, String password, String url, String ipAddress, int port, String type) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.ipAddress = ipAddress;
        this.port = port;
        this.type = type;
    }
}
