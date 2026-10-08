package org.ipcamera.config.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Line {
    private String regname;
    private String ipAddress;
    private int port;
    private String login;
    private String password;

    public Line(String regname, String ipAddress, int port, String login, String password) {
        this.regname = regname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.login = login;
        this.password = password;
    }
}
