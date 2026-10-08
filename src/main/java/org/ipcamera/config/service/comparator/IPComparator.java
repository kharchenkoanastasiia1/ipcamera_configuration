package org.ipcamera.config.service.comparator;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IPComparator {

    /*
    * Сортировка коллекции IP для GUI
    * */
    public static Comparator<String> compareIP(){
        return Comparator.comparing(
                ip -> Arrays.stream(ip.split("\\."))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList()),
                (a, b) -> {
                    for (int i = 0; i < a.size(); i++) {
                        int diff = a.get(i) - b.get(i);
                        if (diff != 0) return diff;
                    }
                    return 0;
                }
        );
    }

    /*
     * Сортировка коллекции IP колонки проблем времени для GUI
     * */
    public static Comparator<String> compareIPWithHighlight(ConfigProcessVariable configProcessVariable, Map<String, Camera> cameraMap){
        return (ip1, ip2) -> {
            Camera c1 = cameraMap.get(ip1);
            Camera c2 = cameraMap.get(ip2);

            int priority1 = 0;
            if (c1 != null) {
                if (c1.getDifferenceTime() > configProcessVariable.getDelta()) {
                    priority1 = 2;
                } else if (!c1.getStatusGetRequest()) {
                    priority1 = 1;
                }
            }

            int priority2 = 0;
            if (c2 != null) {
                if (c2.getDifferenceTime() > configProcessVariable.getDelta()) {
                    priority2 = 2;
                } else if (!c2.getStatusGetRequest()) {
                    priority2 = 1;
                }
            }

            if (priority1 != priority2) {
                return Integer.compare(priority2, priority1);
            }

            List<Integer> ipParts1 = Arrays.stream(ip1.split("\\.")).map(Integer::parseInt).toList();
            List<Integer> ipParts2 = Arrays.stream(ip2.split("\\.")).map(Integer::parseInt).toList();

            for (int i = 0; i < ipParts1.size(); i++) {
                int diff = ipParts1.get(i) - ipParts2.get(i);
                if (diff != 0) return diff;
            }
            return 0;
        };
    }
}
