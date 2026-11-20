package service;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.ipcamera.config.service.time.NTPTimeService;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NTPTimeServiceTest {
    @Test
    void readsServerTransmitTimestampAndClosesClient() throws Exception {
        String host = "127.0.0.1";
        InetAddress address = InetAddress.getByName(host);
        Date expected = Date.from(Instant.parse("2026-09-06T12:00:00Z"));
        TimeInfo info = mock(TimeInfo.class);
        NtpV3Packet packet = mock(NtpV3Packet.class);
        when(info.getMessage()).thenReturn(packet);
        when(packet.getTransmitTimeStamp()).thenReturn(new TimeStamp(expected));

        try (var clients = mockConstruction(NTPUDPClient.class,
                (client, context) -> when(client.getTime(address)).thenReturn(info))) {
            assertEquals(expected, new NTPTimeService().getNTPTime(host));

            NTPUDPClient client = clients.constructed().getFirst();
            var order = inOrder(client, info);
            order.verify(client).setDefaultTimeout(5000);
            order.verify(client).open();
            order.verify(client).getTime(address);
            order.verify(info).computeDetails();
            order.verify(client).close();
        }
    }
}
