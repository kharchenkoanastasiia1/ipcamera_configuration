package org.ipcamera.config.service.time;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.Date;

public class NTPTimeService {

    public NTPTimeService() {
    }

    public Date getNTPTime(String ntpHost) throws Exception {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(5000);     //если сервер не отвечает или сеть медленная
        client.open();
        InetAddress address = InetAddress.getByName(ntpHost);
        TimeInfo info = client.getTime(address);
        info.computeDetails();

        long returnTime = info.getMessage().getTransmitTimeStamp().getTime();
        Date time = new Date(returnTime);

        client.close();
        return time;
    }
}
