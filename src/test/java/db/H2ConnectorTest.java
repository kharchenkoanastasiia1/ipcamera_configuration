package db;

import org.ipcamera.config.db.H2Connector;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.factory.CameraFactory;
import org.ipcamera.config.service.util.CameraUtilsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class H2ConnectorTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private Statement statement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {

        connection =
                mock(Connection.class);

        preparedStatement =
                mock(PreparedStatement.class);

        statement =
                mock(Statement.class);

        resultSet =
                mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(connection.createStatement())
                .thenReturn(statement);

        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
    }

    @AfterEach
    void tearDown() {
        Thread.interrupted();
    }

    // =====================================================================
    // createTableCameras()
    // =====================================================================

    @Test
    void createTableCameras_shouldExecuteCreateStatement()
            throws Exception {

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            H2Connector.createTableCameras();

            verify(connection)
                    .createStatement();

            verify(statement)
                    .execute(anyString());

            verify(connection)
                    .close();
        }
    }

    // =====================================================================
    // addTableCameras()
    // =====================================================================

    @Test
    void addTableCameras_shouldMergeCameraIntoDatabase()
            throws Exception {

        Camera camera =
                createCamera();

        List<Camera> cameras =
                List.of(camera);

        /*
         * addTableCameras сначала вызывает truncateCameras(),
         * а затем снова getConnection().
         *
         * Поэтому возвращаем connection два раза.
         */
        Connection truncateConnection =
                mock(Connection.class);

        Statement truncateStatement =
                mock(Statement.class);

        when(truncateConnection.createStatement())
                .thenReturn(truncateStatement);

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(
                            truncateConnection,
                            connection
                    );

            H2Connector.addTableCameras(cameras);

            verify(truncateStatement)
                    .execute(anyString());

            verify(preparedStatement)
                    .setString(1, "rtsp://192.168.1.100:554/stream");

            verify(preparedStatement)
                    .setString(2, "192.168.1.100");

            verify(preparedStatement)
                    .setInt(3, 554);

            verify(preparedStatement)
                    .setString(4, "admin");

            verify(preparedStatement)
                    .setString(5, "password");

            verify(preparedStatement)
                    .setString(6, "AXIS");

            verify(preparedStatement)
                    .setString(7, "line1,line2");

            verify(preparedStatement)
                    .setString(8, "Camera 1,Camera 2");

            verify(preparedStatement)
                    .setString(9, "transmit1,transmit2");

            verify(preparedStatement)
                    .setString(10, "user1,user2");

            verify(preparedStatement)
                    .setString(11, "pwd1,pwd2");

            verify(preparedStatement)
                    .setString(12, "reg1,reg2");

            verify(preparedStatement)
                    .addBatch();

            verify(preparedStatement)
                    .executeBatch();
        }
    }

    // =====================================================================
    // getCameras()
    // =====================================================================

    @Test
    void getCameras_shouldReadCameraFromDatabase()
            throws Exception {

        Camera camera =
                mock(Camera.class);

        when(resultSet.next())
                .thenReturn(
                        true,
                        false
                );

        when(resultSet.getString("username"))
                .thenReturn("admin");

        when(resultSet.getString("password"))
                .thenReturn("password");

        when(resultSet.getString("url"))
                .thenReturn(
                        "rtsp://192.168.1.100:554/stream"
                );

        when(resultSet.getString("ip_address"))
                .thenReturn(
                        "192.168.1.100"
                );

        when(resultSet.getInt("port"))
                .thenReturn(554);

        when(resultSet.getString("type"))
                .thenReturn("AXIS");

        when(resultSet.getString("line"))
                .thenReturn(
                        "line1,line2"
                );

        when(resultSet.getString("nickname"))
                .thenReturn(
                        "Camera 1,Camera 2"
                );

        when(resultSet.getString("transmiturl"))
                .thenReturn(
                        "url1,url2"
                );

        when(resultSet.getString("transmitusr"))
                .thenReturn(
                        "usr1,usr2"
                );

        when(resultSet.getString("transmitpwd"))
                .thenReturn(
                        "pwd1,pwd2"
                );

        when(resultSet.getString("regname"))
                .thenReturn(
                        "reg1,reg2"
                );

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        );

                MockedStatic<CameraFactory> cameraFactory =
                        mockStatic(CameraFactory.class)
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            cameraFactory.when(() ->
                            CameraFactory.createCameraByType(
                                    "admin",
                                    "password",
                                    "rtsp://192.168.1.100:554/stream",
                                    "192.168.1.100",
                                    554,
                                    "AXIS"
                            )
                    )
                    .thenReturn(camera);

            List<Camera> result =
                    H2Connector.getCameras();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertSame(camera, result.get(0));

            verify(camera)
                    .setLine(
                            new String[]{
                                    "line1",
                                    "line2"
                            }
                    );

            verify(camera)
                    .setNickname(
                            new String[]{
                                    "Camera 1",
                                    "Camera 2"
                            }
                    );

            verify(camera)
                    .setTransmitUrl(
                            new String[]{
                                    "url1",
                                    "url2"
                            }
                    );

            verify(camera)
                    .setTransmitUsr(
                            new String[]{
                                    "usr1",
                                    "usr2"
                            }
                    );

            verify(camera)
                    .setTransmitPsw(
                            new String[]{
                                    "pwd1",
                                    "pwd2"
                            }
                    );

            verify(camera)
                    .setRegname(
                            new String[]{
                                    "reg1",
                                    "reg2"
                            }
                    );
        }
    }

    @Test
    void getCameras_shouldUseEmptyArray_whenDatabaseValueIsNull()
            throws Exception {

        Camera camera =
                mock(Camera.class);

        when(resultSet.next())
                .thenReturn(true, false);

        when(resultSet.getString("username"))
                .thenReturn("admin");

        when(resultSet.getString("password"))
                .thenReturn("password");

        when(resultSet.getString("url"))
                .thenReturn("rtsp://camera");

        when(resultSet.getString("ip_address"))
                .thenReturn("192.168.1.100");

        when(resultSet.getInt("port"))
                .thenReturn(554);

        when(resultSet.getString("type"))
                .thenReturn("AXIS");

        when(resultSet.getString("line"))
                .thenReturn(null);

        when(resultSet.getString("nickname"))
                .thenReturn(null);

        when(resultSet.getString("transmiturl"))
                .thenReturn(null);

        when(resultSet.getString("transmitusr"))
                .thenReturn(null);

        when(resultSet.getString("transmitpwd"))
                .thenReturn(null);

        when(resultSet.getString("regname"))
                .thenReturn(null);

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        );

                MockedStatic<CameraFactory> factory =
                        mockStatic(CameraFactory.class)
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            factory.when(() ->
                            CameraFactory.createCameraByType(
                                    anyString(),
                                    anyString(),
                                    anyString(),
                                    anyString(),
                                    anyInt(),
                                    anyString()
                            )
                    )
                    .thenReturn(camera);

            List<Camera> result =
                    H2Connector.getCameras();

            assertEquals(1, result.size());

            verify(camera)
                    .setLine(
                            argThat(array ->
                                    array.length == 1 &&
                                            "".equals(array[0]))
                    );

            verify(camera)
                    .setNickname(
                            argThat(array ->
                                    array.length == 1 &&
                                            "".equals(array[0]))
                    );
        }
    }

    // =====================================================================
    // Configurations
    // =====================================================================

    @Test
    void addTableConfigurations_shouldWriteAllFields()
            throws Exception {

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        when(config.getUrlDBFirebird())
                .thenReturn(
                        "jdbc:firebirdsql://localhost/db"
                );

        when(config.getNtpHost())
                .thenReturn(
                        "pool.ntp.org"
                );

        when(config.getDelta())
                .thenReturn(30);

        when(config.getThreadCount())
                .thenReturn(5);

        when(config.getIntervalMinutes())
                .thenReturn(10L);

        when(config.getCheckRTSP())
                .thenReturn(true);

        when(config.getAutoUpdateIPCamera())
                .thenReturn(true);

        when(config.getAutoUpdateAxis())
                .thenReturn(false);

        when(config.getAutoUpdateManual())
                .thenReturn(true);

        when(config.getIntervalMinutesManual())
                .thenReturn(15L);

        when(config.getDeltaManual())
                .thenReturn(60);

        when(config.getCheckManual())
                .thenReturn(true);

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            H2Connector.addTableConfigurations(config);

            verify(preparedStatement)
                    .setInt(1, 1);

            verify(preparedStatement)
                    .setString(
                            2,
                            "jdbc:firebirdsql://localhost/db"
                    );

            verify(preparedStatement)
                    .setString(
                            3,
                            "pool.ntp.org"
                    );

            verify(preparedStatement)
                    .setInt(4, 30);

            verify(preparedStatement)
                    .setInt(5, 5);

            verify(preparedStatement)
                    .setLong(6, 10L);

            verify(preparedStatement)
                    .setBoolean(7, true);

            verify(preparedStatement)
                    .setBoolean(8, true);

            verify(preparedStatement)
                    .setBoolean(9, false);

            verify(preparedStatement)
                    .setBoolean(10, true);

            verify(preparedStatement)
                    .setLong(11, 15L);

            verify(preparedStatement)
                    .setInt(12, 60);

            verify(preparedStatement)
                    .setBoolean(13, true);

            verify(preparedStatement)
                    .executeUpdate();
        }
    }

    @Test
    void getConfigProcessVariable_shouldReturnNull_whenTableIsEmpty()
            throws Exception {

        when(resultSet.next())
                .thenReturn(false);

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            ConfigProcessVariable result =
                    H2Connector.getConfigProcessVariable();

            assertNull(result);
        }
    }

    @Test
    void getConfigProcessVariable_shouldReadConfiguration()
            throws Exception {

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getString("url_db_firebird"))
                .thenReturn(
                        "jdbc:firebirdsql://localhost/db"
                );

        when(resultSet.getString("ntp_host"))
                .thenReturn(
                        "pool.ntp.org"
                );

        when(resultSet.getInt("delta"))
                .thenReturn(30);

        when(resultSet.getInt("thread_count"))
                .thenReturn(4);

        when(resultSet.getInt("interval_minutes"))
                .thenReturn(10);

        when(resultSet.getBoolean("check_rtsp"))
                .thenReturn(true);

        when(resultSet.getBoolean("auto_update_ip_camera"))
                .thenReturn(true);

        when(resultSet.getBoolean("auto_update_axis"))
                .thenReturn(false);

        when(resultSet.getBoolean("auto_update_manual"))
                .thenReturn(true);

        when(resultSet.getInt("interval_minutes_manual"))
                .thenReturn(15);

        when(resultSet.getInt("delta_manual"))
                .thenReturn(60);

        when(resultSet.getBoolean("check_manual"))
                .thenReturn(true);

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            ConfigProcessVariable result =
                    H2Connector.getConfigProcessVariable();

            assertNotNull(result);

            assertEquals(
                    "jdbc:firebirdsql://localhost/db",
                    result.getUrlDBFirebird()
            );

            assertEquals(
                    "pool.ntp.org",
                    result.getNtpHost()
            );

            assertEquals(
                    30,
                    result.getDelta()
            );

            assertEquals(
                    4,
                    result.getThreadCount()
            );

            assertEquals(
                    10,
                    result.getIntervalMinutes()
            );

            assertTrue(
                    result.getCheckRTSP()
            );

            assertTrue(
                    result.getAutoUpdateIPCamera()
            );

            assertFalse(
                    result.getAutoUpdateAxis()
            );

            assertTrue(
                    result.getAutoUpdateManual()
            );

            assertEquals(
                    15,
                    result.getIntervalMinutesManual()
            );

            assertEquals(
                    60,
                    result.getDeltaManual()
            );

            assertTrue(
                    result.getCheckManual()
            );
        }
    }

    // =====================================================================
    // Manual
    // =====================================================================

    @Test
    void addTableManual_shouldSaveCameraManualSettings()
            throws Exception {

        Camera camera =
                mock(Camera.class);

        when(camera.getUrl())
                .thenReturn(
                        "rtsp://192.168.1.100"
                );

        when(camera.getManualMode())
                .thenReturn(true);

        when(camera.getBanOnAutoUpdate())
                .thenReturn(false);

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            H2Connector.addTableManual(camera);

            verify(preparedStatement)
                    .setString(
                            1,
                            "rtsp://192.168.1.100"
                    );

            verify(preparedStatement)
                    .setBoolean(
                            2,
                            true
                    );

            verify(preparedStatement)
                    .setBoolean(
                            3,
                            false
                    );

            verify(preparedStatement)
                    .executeUpdate();
        }
    }

    @Test
    void getManual_shouldUpdateMatchingCamera()
            throws Exception {

        Camera camera =
                mock(Camera.class);

        List<Camera> cameras =
                new ArrayList<>();

        cameras.add(camera);

        when(resultSet.next())
                .thenReturn(
                        true,
                        false
                );

        when(resultSet.getString("url"))
                .thenReturn(
                        "rtsp://192.168.1.100"
                );

        when(resultSet.getBoolean("manual_mode"))
                .thenReturn(true);

        when(resultSet.getBoolean("ban_on_autoupdate"))
                .thenReturn(true);

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        );

                MockedStatic<CameraUtilsService> cameraUtils =
                        mockStatic(CameraUtilsService.class)
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            cameraUtils.when(() ->
                            CameraUtilsService.getCameraByUrl(
                                    cameras,
                                    "rtsp://192.168.1.100"
                            )
                    )
                    .thenReturn(camera);

            List<Camera> result =
                    H2Connector.getManual(cameras);

            assertSame(cameras, result);

            verify(camera)
                    .setManualMode(true);

            verify(camera)
                    .setBanOnAutoUpdate(true);
        }
    }

    // =====================================================================
    // Lines
    // =====================================================================

    @Test
    void addTableLines_shouldInsertLines()
            throws Exception {

        Line line1 =
                mock(Line.class);

        Line line2 =
                mock(Line.class);

        when(line1.getRegname())
                .thenReturn("REG-1");

        when(line1.getIpAddress())
                .thenReturn("192.168.1.10");

        when(line1.getPort())
                .thenReturn(10001);

        when(line1.getLogin())
                .thenReturn("admin1");

        when(line1.getPassword())
                .thenReturn("pass1");

        when(line2.getRegname())
                .thenReturn("REG-2");

        when(line2.getIpAddress())
                .thenReturn("192.168.1.11");

        when(line2.getPort())
                .thenReturn(10002);

        when(line2.getLogin())
                .thenReturn("admin2");

        when(line2.getPassword())
                .thenReturn("pass2");

        Connection truncateConnection =
                mock(Connection.class);

        Statement truncateStatement =
                mock(Statement.class);

        when(truncateConnection.createStatement())
                .thenReturn(truncateStatement);

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(
                            truncateConnection,
                            connection
                    );

            H2Connector.addTableLines(
                    List.of(
                            line1,
                            line2
                    )
            );

            verify(truncateStatement)
                    .execute(anyString());

            verify(preparedStatement, times(2))
                    .addBatch();

            verify(preparedStatement)
                    .executeBatch();

            verify(preparedStatement)
                    .setString(1, "REG-1");

            verify(preparedStatement)
                    .setString(1, "REG-2");

            verify(preparedStatement)
                    .setInt(3, 10001);

            verify(preparedStatement)
                    .setInt(3, 10002);
        }
    }

    @Test
    void getLines_shouldReturnValidLines()
            throws Exception {

        when(resultSet.next())
                .thenReturn(
                        true,
                        true,
                        false
                );

        when(resultSet.getString("regname"))
                .thenReturn(
                        "REG-1",
                        "REG-2"
                );

        when(resultSet.getString("ip_address"))
                .thenReturn(
                        "192.168.1.10",
                        "192.168.1.11"
                );

        when(resultSet.getInt("port"))
                .thenReturn(
                        10001,
                        10002
                );

        when(resultSet.getString("login"))
                .thenReturn(
                        "admin1",
                        "admin2"
                );

        when(resultSet.getString("password"))
                .thenReturn(
                        "pass1",
                        "pass2"
                );

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            List<Line> result =
                    H2Connector.getLines();

            assertNotNull(result);
            assertEquals(
                    2,
                    result.size()
            );

            assertEquals(
                    "REG-1",
                    result.get(0).getRegname()
            );

            assertEquals(
                    "192.168.1.10",
                    result.get(0).getIpAddress()
            );

            assertEquals(
                    10001,
                    result.get(0).getPort()
            );

            assertEquals(
                    "admin1",
                    result.get(0).getLogin()
            );

            assertEquals(
                    "pass1",
                    result.get(0).getPassword()
            );
        }
    }

    @Test
    void getLines_shouldSkipInvalidLine()
            throws Exception {

        when(resultSet.next())
                .thenReturn(
                        true,
                        false
                );

        when(resultSet.getString("regname"))
                .thenReturn("REG-1");

        when(resultSet.getString("ip_address"))
                .thenReturn(null);

        when(resultSet.getInt("port"))
                .thenReturn(10001);

        when(resultSet.getString("login"))
                .thenReturn("admin");

        when(resultSet.getString("password"))
                .thenReturn("password");

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            List<Line> result =
                    H2Connector.getLines();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // =====================================================================
    // SQLException
    // =====================================================================

    @Test
    void getCameras_shouldThrowRuntimeException_whenSQLExceptionOccurs()
            throws Exception {

        when(connection.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException(
                                "H2 error"
                        )
                );

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            assertThrows(
                    RuntimeException.class,
                    H2Connector::getCameras
            );
        }
    }

    @Test
    void getLines_shouldThrowRuntimeException_whenSQLExceptionOccurs()
            throws Exception {

        when(connection.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException(
                                "H2 line error"
                        )
                );

        try (
                MockedStatic<H2Connector> h2 =
                        mockStatic(
                                H2Connector.class,
                                CALLS_REAL_METHODS
                        )
        ) {

            h2.when(H2Connector::getConnection)
                    .thenReturn(connection);

            assertThrows(
                    RuntimeException.class,
                    H2Connector::getLines
            );
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private Camera createCamera() {

        Camera camera =
                mock(Camera.class);

        when(camera.getUrl())
                .thenReturn(
                        "rtsp://192.168.1.100:554/stream"
                );

        when(camera.getIpAddress())
                .thenReturn(
                        "192.168.1.100"
                );

        when(camera.getPort())
                .thenReturn(
                        554
                );

        when(camera.getUsername())
                .thenReturn(
                        "admin"
                );

        when(camera.getPassword())
                .thenReturn(
                        "password"
                );

        when(camera.getType())
                .thenReturn(
                        "AXIS"
                );

        when(camera.getLine())
                .thenReturn(
                        new String[]{
                                "line1",
                                "line2"
                        }
                );

        when(camera.getNickname())
                .thenReturn(
                        new String[]{
                                "Camera 1",
                                "Camera 2"
                        }
                );

        when(camera.getTransmitUrl())
                .thenReturn(
                        new String[]{
                                "transmit1",
                                "transmit2"
                        }
                );

        when(camera.getTransmitUsr())
                .thenReturn(
                        new String[]{
                                "user1",
                                "user2"
                        }
                );

        when(camera.getTransmitPsw())
                .thenReturn(
                        new String[]{
                                "pwd1",
                                "pwd2"
                        }
                );

        when(camera.getRegname())
                .thenReturn(
                        new String[]{
                                "reg1",
                                "reg2"
                        }
                );

        return camera;
    }
}
