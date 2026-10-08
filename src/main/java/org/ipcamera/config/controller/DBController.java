package org.ipcamera.config.controller;

import org.ipcamera.config.db.FirebirdConnector;
import org.ipcamera.config.db.H2Connector;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serilogj.Log;

import java.util.List;

import static org.ipcamera.config.constants.ConstantsLogger.DB_UNAVAILABLE;
import static org.ipcamera.config.constants.ConstantsLogger.NAME_PROGRAM_SEQ;

public class DBController {
    private static final Logger logger = LoggerFactory.getLogger(DBController.class);

    public DBController() {}

    public List<Camera> getCamerasList(ConfigProcessVariable configProcessVariable){
        List<Camera> cameras = FirebirdConnector.getListCameras(configProcessVariable);
        if(cameras != null){
            H2Connector.createTableCameras();
            H2Connector.addTableCameras(cameras);
        } else{
            cameras = H2Connector.getCameras();
            Log.information(NAME_PROGRAM_SEQ + DB_UNAVAILABLE);
            logger.info(DB_UNAVAILABLE);
        }

        return cameras;
    }

    public ConfigProcessVariable getConfiguration(){
        H2Connector.createTableConfigurations();
        ConfigProcessVariable configProcessVariable = H2Connector.getConfigProcessVariable();
        if(configProcessVariable == null){
            configProcessVariable = new ConfigProcessVariable();
            H2Connector.addTableConfigurations(configProcessVariable);
        }
        return configProcessVariable;
    }

    public List<Camera> getManual(List<Camera> camerasWithoutManual){
        H2Connector.createTableManual();
        return H2Connector.getManual(camerasWithoutManual);
    }

    public List<Line> getLines(ConfigProcessVariable configProcessVariable){
        List<Line> lines = FirebirdConnector.getListLines(configProcessVariable);
        if(lines != null){
            H2Connector.createTableLines();
            H2Connector.addTableLines(lines);
        } else{
            lines = H2Connector.getLines();
            Log.information(NAME_PROGRAM_SEQ + DB_UNAVAILABLE);
            logger.info(DB_UNAVAILABLE);
        }

        return lines;
    }
}
