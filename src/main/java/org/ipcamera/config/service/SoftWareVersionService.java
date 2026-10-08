package org.ipcamera.config.service;

public class SoftWareVersionService {

    public SoftWareVersionService() {}

    public int compareVersion(String version) {
        if (version == null || version.isEmpty() || version.equals("null") ) {
            return 0;
        }
        if(compareTwoVersions(version, "9.30") == 0){
            return 0;           //не пойми где этот диапазон
        }
        if(compareTwoVersions(version, "6.0") == -1){
            return 3;           //тип запросов меньше 6 (5.20 и 5.50);
        } else if(compareTwoVersions(version, "9.30") == -1){
            return 1;       //тип запросов до 9.30
        } else if(compareTwoVersions(version, "12.4") == -1){
            return 2;           //тип запросов с 9.3 до 12.4;
        } else{
            return 0;           //тип запросов не определен
        }

//        if(compareTwoVersions(version, "9.30") == -1){
//            return 1;       //тип запросов до 9.30
//        } else if(compareTwoVersions(version, "12.4") == -1){
//            return 2;           //тип запросов с 9.3 до 12.4;
//        } else if(compareTwoVersions(version, "6.0") == -1){
//            return 3;           //тип запросов меньше 6 (5.20 и 5.50);
//        } else{
//            return 0;           //тип запросов не определен
//        }
    }

    private int compareTwoVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }
}
