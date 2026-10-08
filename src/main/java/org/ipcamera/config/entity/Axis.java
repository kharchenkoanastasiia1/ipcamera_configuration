package org.ipcamera.config.entity;

import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;

public class Axis extends Camera{

    public Axis(String username, String password, String url) {
        super(username, password, url);
        this.setType(TYPE_AXIS);
    }

    public Axis(String username, String password, String url, String ipAddress, int port, String type) {
        super(username, password, url, ipAddress, port, type);
    }
}
