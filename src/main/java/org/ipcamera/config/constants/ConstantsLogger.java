package org.ipcamera.config.constants;

public class ConstantsLogger {
    public static final String START_CHECK = "Початок перевірки.";
    public static final String START_AUTOUPDATE = " Запущено процес для автооновлення.";
    public static final String STOP_CHECK = "Перевірку завершено.";
    public static final String TIME_SCHEDULER_STOPPED = "Планувальний зупинено через зміну інтервалу запуску задач.";
    public static final String FORCED_STOP_THREADS = "Не всі потоки завершилися за {} хвилин – примусова зупинка";
    public static final String INTERRUPT_WHILE_STOP = "Перервано під час зупинки планувальника.";

    public static final String PING_DONT_REACH = "PING: {} недоступний.";
    public static final String PING_RTSP_DONT_REACH = "RTSP: {} недоступний.";

    public static final String CONNECTION_ERROR = "Помилка підключення: сервер недоступний або неправильна адреса. IP: {}. Версія ПЗ: {}";
    public static final String FAILED_HOST = "Не вдалося дозволити хост: {}. Версія ПЗ: {}";
    public static final String ERROR_EXECUTE_REQUEST = "Помилка введення/виводу під час виконання запиту. IP: {}. Версія ПЗ: {}";
    public static final String ERROR_UNEXPECTED = "Відбулася непередбачена помилка. IP: {}. Версія ПЗ: {}";
    public static final String ERROR_PARSE_DATE = "Не вдалося проаналізувати час сервера. IP: {}. Версія ПЗ: {}";

    public static final String TIME_CHANGE_SUCCESS = "Час у {} успішно змінено.";
    public static final String TIMEZONE_CHANGE_SUCCESS = "Часова зона у {} успішно змінено.";
    public static final String SUCCESS_UPDATE_LINE = "Лінію {} успішно оновлено.";
    public static final String FAILED_UPDATE_LINE = "Не вдалося оновити лінію {}. {}";

    public static final String PROPERTIES_FILE_NOT_FOUND = "Файл властивостей не знайдено: {}";
    public static final String ERROR_LOAD_DB_CONFIGURATION = "Помилка завантаження конфігурації БД: {}";
    public static final String ERROR_CREATE_TABLE_H2 = "Помилка під час створення таблиці: {}";
    public static final String ERROR_ALTER_TABLE_H2 = "Помилка під час заповнення таблиці: {}";
    public static final String ERROR_READ_TABLE_H2 = "Помилка під час читання з таблиці: {}";

    public static final String NAME_PROGRAM_SEQ = "H15 TimeControl ";

    public static final String PING_DONT_REACH_SEQ = "PING: %s недоступний.";
    public static final String PING_RTSP_DONT_REACH_SEQ = "RTSP: %s недоступний.";

    public static final String CONNECTION_ERROR_SEQ = "Помилка підключення: сервер недоступний або неправильна адреса. IP: %s. Версія ПЗ: %s";
    public static final String FAILED_HOST_SEQ = "Не вдалося дозволити хост: %s. Версія ПЗ: %s";
    public static final String ERROR_EXECUTE_REQUEST_SEQ = "Помилка введення/виводу під час виконання запиту. IP: %s. Версія ПЗ: %s";
    public static final String ERROR_UNEXPECTED_SEQ = "Відбулася непередбачена помилка. IP: %s. Версія ПЗ: %s";
    public static final String ERROR_PARSE_DATE_SEQ = "Не вдалося проаналізувати час сервера. IP: %s. Версія ПЗ: %s";
    public static final String FORCED_STOP_THREADS_SEQ = "Не всі потоки завершилися за %s хвилин – примусова зупинка";

    public static final String TIME_CHANGE_SUCCESS_SEQ = "Час у %s успішно змінено.";
    public static final String TIMEZONE_CHANGE_SUCCESS_SEQ = "Часова зона у %s успішно змінено.";
    public static final String SUCCESS_UPDATE_LINE_SEQ = "Лінію %s успішно оновлено.";
    public static final String FAILED_UPDATE_LINE_SEQ = "Не вдалося оновити лінію %s. Спроба %s.";

    public static final String PROPERTIES_FILE_NOT_FOUND_SEQ = "Файл властивостей не знайдено: %s";
    public static final String ERROR_LOAD_DB_CONFIGURATION_SEQ = "Помилка завантаження конфігурації БД: %s";
    public static final String ERROR_CREATE_TABLE_H2_SEQ = "Помилка під час створення таблиці: %s";
    public static final String ERROR_ALTER_TABLE_H2_SEQ = "Помилка під час заповнення таблиці: %s";
    public static final String ERROR_READ_TABLE_H2_SEQ = "Помилка під час читання з таблиці: %s";
    public static final String DB_UNAVAILABLE = "Основна БД недоступна. Дані камер отримані з кеша.";

    public static final String NOT_EMPTY = " не може бути порожнім.\n";
    public static final String GREATER_THAN_0 = " має бути більше 0.\n";
    public static final String MUST_BE_NUMBER = " має бути числом.\n";
    public static final String DATA_ENTRY_ERROR = "Помилка введення даних";
    public static final String INCORRECT_FIELD_VALUE = "Некоректні значення полів";
    public static final String INCORRECT_INTERVAL_DIFFERENCE = "Різниця часу перезапуску потоків для повної колекції з БД та режиму manual повинна бути більшою за 20 хв";

    public static final String INCORRECT_FIREBIRD_URL_FORMAT = "Некоректний формат URL Firebird: ";
    public static final String INCORRECT_FIREBIRD_URL_FORMAT_MISSING = "Некоректний формат URL Firebird: відсутня '/' після порту";
    public static final String INCORRECT_FIREBIRD_READ = "Помилка читання з Firebird: {}";
    public static final String INCORRECT_FIREBIRD_READ_SEQ = "Помилка читання з Firebird: %s";

    public static final String UNABLE_DETERMINE_APPLICATION_PATH = "Не вдалося визначити шлях додатку";
}
