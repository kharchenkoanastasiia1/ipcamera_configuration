package org.ipcamera.config.controller;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.repository.lines_request.LineRepository;
import org.ipcamera.config.service.util.LineUtilsService;

import java.util.List;

public class LineUpdateController {

    public LineUpdateController() {}

    /**
     * Обновить линии для текущей камеры
     * */
    public void updateLines(List<Line> lines, Camera camera) {
        if (lines == null || camera == null) {
            return;
        }

        for (int i = 0; i < camera.getLine().length; i++) {
            updateLine(camera.getLine()[i], LineUtilsService.getLineByRegname(lines, camera.getRegname()[i]));
        }
    }

    /**
     * Обновить конкретную линию
     * */
    public void updateLine(String lineId, Line line) {
        LineRepository lineRepository = new LineRepository();
        lineRepository.restart(lineId, line);
    }
}
