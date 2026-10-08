package org.ipcamera.config.entity;

import static org.ipcamera.config.constants.ConstantsType.TYPE_IP_CAMERA;

public class IPCamera extends Camera{
    public IPCamera(String username, String password, String url) {
        super(username, password, url);
        this.setType(TYPE_IP_CAMERA);
    }

    public IPCamera(String username, String password, String url, String ipAddress, int port, String type) {
        super(username, password, url, ipAddress, port, type);
    }
}
