package db;

import org.ipcamera.config.db.FirebirdConnector;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.factory.CameraFactory;
import org.ipcamera.config.service.ExtractFromURLService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FirebirdConnectorTest {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    private ConfigProcessVariable config;

    @BeforeEach
    void setUp() throws Exception {

        connection = mock(Connection.class);
        statement = mock(Statement.class);
        resultSet = mock(ResultSet.class);

        config = mock(ConfigProcessVariable.class);

        when(config.getUrlDBFirebird())
                .thenReturn("jdbc:firebirdsql://localhost/test.fdb");

        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);
    }

    @AfterEach
    void tearDown() {
        Thread.interrupted();
    }

    // =====================================================================
    // getListCameras()
    // =====================================================================

    @Test
    void getListCameras_shouldReturnCamera_whenDatabaseContainsValidRow()
            throws Exception {

        Camera camera = mock(Camera.class);

        when(resultSet.next())
                .thenReturn(true, false);

        when(resultSet.getString("MEDIAURL"))
                .thenReturn("rtsp://192.168.1.100:554/stream");

        when(resultSet.getString("MEDIAUSR"))
                .thenReturn("admin");

        when(resultSet.getString("MEDIAPWD"))
                .thenReturn("password");

        when(resultSet.getString("LINE"))
                .thenReturn("line1");

        when(resultSet.getString("NICKNAME"))
                .thenReturn("Camera 1");

        when(resultSet.getString("TRANSMITURL"))
                .thenReturn("rtsp://transmit");

        when(resultSet.getString("TRANSMITUSR"))
                .thenReturn("transmit-user");

        when(resultSet.getString("TRANSMITPWD"))
                .thenReturn("transmit-password");

        when(resultSet.getString("REGNAME"))
                .thenReturn("reg1");

        try (
                MockedStatic<FirebirdConnector> firebird =
                        mockStatic(
                                FirebirdConnector.class,
                                CALLS_REAL_METHODS
                        );

                MockedStatic<ExtractFromURLService> extract =
                        mockStatic(ExtractFromURLService.class);

                MockedStatic<CameraFactory> cameraFactory =
                        mockStatic(CameraFactory.class)
        ) {

            firebird.when(FirebirdConnector::getConnection)
                    .thenReturn(connection);

            extract.when(() ->
                            ExtractFromURLService.extractIP(
                                    "rtsp://192.168.1.100:554/stream"
                            )
                    )
                    .thenReturn("192.168.1.100");

            extract.when(() ->
                            ExtractFromURLService.extractPort(
                                    "rtsp://192.168.1.100:554/stream"
                            )
                    )
                    .thenReturn(554);

            cameraFactory.when(() ->
                            CameraFactory.createCamera(
                                    "rtsp://192.168.1.100:554/stream",
                                    "admin",
                                    "password"
                            )
                    )
                    .thenReturn(camera);

            List<Camera> result =
                    FirebirdConnector.getListCameras(config);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertSame(camera, result.get(0));

            verify(camera)
                    .setIpAddress("192.168.1.100");

            verify(camera)
                    .setPort(554);

            verify(camera)
                    .setLine(
                            argThat(array ->
                                    array.length == 1 &&
                                            "line1".equals(array[0]))
                    );

            verify(camera)
                    .setNickname(
                            argThat(array ->
                                    array.length == 1 &&
                                            "Camera 1".equals(array[0]))
                    );

            verify(camera)
                    .setTransmitUrl(
                            argThat(array ->
                                    array.length == 1 &&
                                            "rtsp://transmit".equals(array[0]))
                    );

            verify(camera)
                    .setTransmitUsr(
                            argThat(array ->
                                    array.length == 1 &&
                                            "transmit-user".equals(array[0]))
                    );

            verify(camera)
                    .setTransmitPsw(
                            argThat(array ->
                                    array.length == 1 &&
                                            "transmit-password".equals(array[0]))
                    );

            verify(camera)
                    .setRegname(
                            argThat(array ->
                                    array.length == 1 &&
                                            "reg1".equals(array[0]))
                    );

            verify(connection).close();
            verify(statement).close();
            verify(resultSet).close();
        }
    }

    @Test
    void getListCameras_shouldSkipDeletedCamera()
            throws Exception {

        when(resultSet.next())
                .thenReturn(true, false);

        when(resultSet.getString("MEDIAURL"))
                .thenReturn("deleted");

        try (
                MockedStatic<FirebirdConnector> firebird =
                        mockStatic(
                                FirebirdConnector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            firebird.when(FirebirdConnector::getConnection)
                    .thenReturn(connection);

            List<Camera> result =
                    FirebirdConnector.getListCameras(config);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(resultSet, never())
                    .getString("MEDIAUSR");

            verify(resultSet, never())
                    .getString("MEDIAPWD");
        }
    }

    @Test
    void getListCameras_shouldSkipRow_whenCredentialsAreNull()
            throws Exception {

        when(resultSet.next())
                .thenReturn(true, false);

        when(resultSet.getString("MEDIAURL"))
                .thenReturn("rtsp://192.168.1.100:554/stream");

        when(resultSet.getString("MEDIAUSR"))
                .thenReturn(null);

        when(resultSet.getString("MEDIAPWD"))
                .thenReturn("password");

        try (
                MockedStatic<FirebirdConnector> firebird =
                        mockStatic(
                                FirebirdConnector.class,
                                CALLS_REAL_METHODS
                        );

                MockedStatic<CameraFactory> cameraFactory =
                        mockStatic(CameraFactory.class)
        ) {

            firebird.when(FirebirdConnector::getConnection)
                    .thenReturn(connection);

            List<Camera> result =
                    FirebirdConnector.getListCameras(config);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            cameraFactory.verifyNoInteractions();
        }
    }

    @Test
    void getListCameras_shouldMergeRowsWithSameIp()
            throws Exception {

        Camera camera = mock(Camera.class);

        when(resultSet.next())
                .thenReturn(true, true, false);

        when(resultSet.getString("MEDIAURL"))
                .thenReturn(
                        "rtsp://192.168.1.100:554/stream1",
                        "rtsp://192.168.1.100:554/stream2"
                );

        when(resultSet.getString("MEDIAUSR"))
                .thenReturn("admin");

        when(resultSet.getString("MEDIAPWD"))
                .thenReturn("password");

        when(resultSet.getString("LINE"))
                .thenReturn(
                        "line1",
                        "line2"
                );

        when(resultSet.getString("NICKNAME"))
                .thenReturn(
                        "Camera 1",
                        "Camera 2"
                );

        when(resultSet.getString("TRANSMITURL"))
                .thenReturn(null);

        when(resultSet.getString("TRANSMITUSR"))
                .thenReturn(null);

        when(resultSet.getString("TRANSMITPWD"))
                .thenReturn(null);

        when(resultSet.getString("REGNAME"))
                .thenReturn(
                        "reg1",
                        "reg2"
                );

        /*
         * После первой строки production-код установит:
         * line = {"line1"}
         *
         * Для второй строки вызывается getLine().
         */
        when(camera.getLine())
                .thenReturn(new String[]{"line1"});

        when(camera.getNickname())
                .thenReturn(new String[]{"Camera 1"});

        when(camera.getTransmitUrl())
                .thenReturn(new String[]{""});

        when(camera.getTransmitUsr())
                .thenReturn(new String[]{""});

        when(camera.getTransmitPsw())
                .thenReturn(new String[]{""});

        when(camera.getRegname())
                .thenReturn(new String[]{"reg1"});

        try (
                MockedStatic<FirebirdConnector> firebird =
                        mockStatic(
                                FirebirdConnector.class,
                                CALLS_REAL_METHODS
                        );

                MockedStatic<ExtractFromURLService> extract =
                        mockStatic(ExtractFromURLService.class);

                MockedStatic<CameraFactory> cameraFactory =
                        mockStatic(CameraFactory.class)
        ) {

            firebird.when(FirebirdConnector::getConnection)
                    .thenReturn(connection);

            extract.when(() ->
                            ExtractFromURLService.extractIP(
                                    anyString()
                            )
                    )
                    .thenReturn("192.168.1.100");

            extract.when(() ->
                            ExtractFromURLService.extractPort(
                                    anyString()
                            )
                    )
                    .thenReturn(554);

            cameraFactory.when(() ->
                            CameraFactory.createCamera(
                                    anyString(),
                                    eq("admin"),
                                    eq("password")
                            )
                    )
                    .thenReturn(camera);

            List<Camera> result =
                    FirebirdConnector.getListCameras(config);

            assertNotNull(result);
            assertEquals(1, result.size());

            /*
             * Camera создается только для первой строки.
             */
            cameraFactory.verify(
                    () -> CameraFactory.createCamera(
                            anyString(),
                            eq("admin"),
                            eq("password")
                    ),
                    times(1)
            );

            verify(camera).setLine(
                    argThat(array ->
                            array.length == 2 &&
                                    "line1".equals(array[0]) &&
                                    "line2".equals(array[1]))
            );

            verify(camera).setNickname(
                    argThat(array ->
                            array.length == 2 &&
                                    "Camera 1".equals(array[0]) &&
                                    "Camera 2".equals(array[1]))
            );

            verify(camera).setRegname(
                    argThat(array ->
                            array.length == 2 &&
                                    "reg1".equals(array[0]) &&
                                    "reg2".equals(array[1]))
            );
        }
    }

    @Test
    void getListCameras_shouldReturnNull_whenSQLExceptionOccurs()
            throws Exception {

        when(statement.executeQuery(anyString()))
                .thenThrow(
                        new SQLException("Database error")
                );

        try (
                MockedStatic<FirebirdConnector> firebird =
                        mockStatic(
                                FirebirdConnector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            firebird.when(FirebirdConnector::getConnection)
                    .thenReturn(connection);

            List<Camera> result =
                    FirebirdConnector.getListCameras(config);

            assertNull(result);
        }
    }

    // =====================================================================
    // getListLines()
    // =====================================================================

    @Test
    void getListLines_shouldReturnLines()
            throws Exception {

        when(resultSet.next())
                .thenReturn(
                        true,
                        true,
                        false
                );

        when(resultSet.getString("REGNAME"))
                .thenReturn(
                        "REG-1",
                        "REG-2"
                );

        when(resultSet.getString("IPADDR"))
                .thenReturn(
                        "192.168.1.10",
                        "192.168.1.11"
                );

        when(resultSet.getInt("IPPORT"))
                .thenReturn(
                        10001,
                        10002
                );

        when(resultSet.getString("CMD_USR"))
                .thenReturn(
                        "admin1",
                        "admin2"
                );

        when(resultSet.getString("CMD_PWD"))
                .thenReturn(
                        "pass1",
                        "pass2"
                );

        try (
                MockedStatic<FirebirdConnector> firebird =
                        mockStatic(
                                FirebirdConnector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            firebird.when(FirebirdConnector::getConnection)
                    .thenReturn(connection);

            List<Line> result =
                    FirebirdConnector.getListLines(config);

            assertNotNull(result);
            assertEquals(2, result.size());

            Line line1 = result.get(0);
            Line line2 = result.get(1);

            assertEquals("REG-1", line1.getRegname());
            assertEquals("192.168.1.10", line1.getIpAddress());
            assertEquals(10001, line1.getPort());
            assertEquals("admin1", line1.getLogin());
            assertEquals("pass1", line1.getPassword());

            assertEquals("REG-2", line2.getRegname());
            assertEquals("192.168.1.11", line2.getIpAddress());
            assertEquals(10002, line2.getPort());
            assertEquals("admin2", line2.getLogin());
            assertEquals("pass2", line2.getPassword());
        }
    }

    @Test
    void getListLines_shouldSkipInvalidRows()
            throws Exception {

        when(resultSet.next())
                .thenReturn(
                        true,
                        false
                );

        when(resultSet.getString("REGNAME"))
                .thenReturn("REG-1");

        when(resultSet.getString("IPADDR"))
                .thenReturn(null);

        when(resultSet.getInt("IPPORT"))
                .thenReturn(10001);

        when(resultSet.getString("CMD_USR"))
                .thenReturn("admin");

        when(resultSet.getString("CMD_PWD"))
                .thenReturn("password");

        try (
                MockedStatic<FirebirdConnector> firebird =
                        mockStatic(
                                FirebirdConnector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            firebird.when(FirebirdConnector::getConnection)
                    .thenReturn(connection);

            List<Line> result =
                    FirebirdConnector.getListLines(config);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getListLines_shouldReturnNull_whenSQLExceptionOccurs()
            throws Exception {

        when(statement.executeQuery(anyString()))
                .thenThrow(
                        new SQLException("Firebird error")
                );

        try (
                MockedStatic<FirebirdConnector> firebird =
                        mockStatic(
                                FirebirdConnector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            firebird.when(FirebirdConnector::getConnection)
                    .thenReturn(connection);

            List<Line> result =
                    FirebirdConnector.getListLines(config);

            assertNull(result);
        }
    }
}
