package controller;

import org.ipcamera.config.controller.CameraTimeController;
import org.ipcamera.config.controller.LineUpdateController;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.repository.CameraRepository;
import org.ipcamera.config.repository.cameras_request.AxisRepository;
import org.ipcamera.config.repository.cameras_request.IPCameraRepository;
import org.ipcamera.config.service.time.NTPTimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;
import java.util.List;

import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;
import static org.ipcamera.config.constants.ConstantsType.TYPE_IP_CAMERA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CameraTimeControllerTest {
    private static final String HOST = "127.0.0.1";
    private final ConfigProcessVariable config = mock(ConfigProcessVariable.class);
    private final CameraTimeController controller = new CameraTimeController(config);
    private final Camera camera = mock(Camera.class);

    @ParameterizedTest
    @ValueSource(strings = {"5.50", "8.50", "10.0"})
    void readsAndUpdatesAxisUsingFirmwareProtocol(String version) throws Exception {
        Date ntpDate = new Date(60_000);
        when(camera.getType()).thenReturn(TYPE_AXIS);
        when(camera.getVersion()).thenReturn(null, version);
        when(camera.getIpAddress()).thenReturn(HOST);
        when(camera.getPort()).thenReturn(554);
        when(config.getDelta()).thenReturn(1);
        when(config.getCheckRTSP()).thenReturn(true);
        List<Line> lines = List.of();

        try (var ping = mockConstruction(CameraRepository.class, (repository, context) -> {
                 when(repository.sendPingRequest(HOST)).thenReturn(true);
                 when(repository.sendPingRtsp(HOST, 554, null, null)).thenReturn(true);
             });
             var ntp = mockConstruction(NTPTimeService.class,
                     (service, context) -> when(service.getNTPTime(HOST)).thenReturn(ntpDate));
             var axis = mockConstruction(AxisRepository.class, (repository, context) -> {
                 when(repository.getVersion(camera)).thenReturn(version);
                 when(repository.getDateTimeForVersionBelow6(camera)).thenReturn(ntpDate);
                 when(repository.getServerDateTime(camera, HOST)).thenReturn(ntpDate);
                 when(repository.postGetDateTime(camera)).thenReturn(ntpDate);
             });
             var lineUpdates = mockConstruction(LineUpdateController.class)) {
            assertFalse(controller.checkRelevanceTime(camera, HOST));
            verify(camera).setVersion(version);
            verify(camera).setStatusPing(true);
            verify(camera).setStatusRtsp(true);
            verify(camera).setDateCheck(ntpDate);
            verify(camera).setDifferenceTime(0);
            AxisRepository reader = axis.constructed().getFirst();
            switch (version) {
                case "5.50" -> verify(reader).getDateTimeForVersionBelow6(camera);
                case "8.50" -> verify(reader).getServerDateTime(camera, HOST);
                case "10.0" -> verify(reader).postGetDateTime(camera);
                default -> fail("Unexpected fixture firmware");
            }

            controller.changeTimeDate(camera, HOST, false, lines);
            AxisRepository writer = axis.constructed().get(1);
            var requestOrder = inOrder(writer);
            if (version.equals("10.0")) {
                requestOrder.verify(writer).postTimeZoneNew(camera);
                requestOrder.verify(writer).postChangeDatetime(camera, HOST, false);
            } else {
                requestOrder.verify(writer).postTimeZoneOld(camera);
                requestOrder.verify(writer).getConfigurationDateTime(camera, HOST, false);
            }
            requestOrder.verifyNoMoreInteractions();
            verify(lineUpdates.constructed().getFirst()).updateLines(lines, camera);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void automaticIpCameraUpdateRequiresPermissionAndOutOfSyncClock(boolean banned) throws Exception {
        Date ntpDate = new Date(120_000);
        when(camera.getType()).thenReturn(TYPE_IP_CAMERA);
        when(camera.getIpAddress()).thenReturn(HOST);
        when(camera.getBanOnAutoUpdate()).thenReturn(banned);
        when(config.getAutoUpdateIPCamera()).thenReturn(true);
        when(config.getCheckRTSP()).thenReturn(false);
        when(config.getDelta()).thenReturn(1);

        try (var ping = mockConstruction(CameraRepository.class,
                     (repository, context) -> when(repository.sendPingRequest(HOST)).thenReturn(true));
             var ntp = mockConstruction(NTPTimeService.class,
                     (service, context) -> when(service.getNTPTime(HOST)).thenReturn(ntpDate));
             var ip = mockConstruction(IPCameraRepository.class, (repository, context) -> {
                 when(repository.getVersion(camera)).thenReturn("1.0");
                 when(repository.getServerTime(camera)).thenReturn(new Date(0));
             })) {
            controller.changeTimeDateAutoUpdate(camera, HOST, true, List.of());

            if (banned) {
                assertTrue(ip.constructed().isEmpty());
                assertTrue(ping.constructed().isEmpty());
            } else {
                verify(camera).setVersion("1.0");
                verify(camera).setDifferenceTime(2);
                verify(ip.constructed().get(1)).postServerTime(camera, HOST, true);
            }
        }
    }

    @Test
    void unreachableCameraSkipsFirmwareAndNtpRequests() throws Exception {
        when(camera.getIpAddress()).thenReturn(HOST);
        when(camera.getLine()).thenReturn(new String[0]);
        try (var ping = mockConstruction(CameraRepository.class,
                     (repository, context) -> when(repository.sendPingRequest(HOST)).thenReturn(false));
             var ntp = mockConstruction(NTPTimeService.class)) {
            assertFalse(controller.checkRelevanceTime(camera, HOST));
            verify(camera).setStatusPing(false);
            verify(camera, never()).setDateCheck(any());
            assertTrue(ntp.constructed().isEmpty());
        }
    }
}
