package org.ipcamera.config.service.util;

import org.ipcamera.config.entity.Line;

import java.util.List;

public class LineUtilsService {

    public static Line getLineByRegname(List<Line> lines, String regname) {
        if (lines == null || regname == null) {
            return null;
        }

        for (Line line : lines) {
            if (regname.equals(line.getRegname())) {
                return line;
            }
        }

        return null;
    }
}
