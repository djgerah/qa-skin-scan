package qa.startup.skinscan.testdata;

import java.util.Base64;
import java.util.UUID;

/**
 * Тестовые данные для загрузки фото.
 * Отделены от PhotoClient, чтобы клиент занимался только HTTP-вызовами.
 */
public final class Picture {

    /**
     * MIME-тип (Multipurpose Internet Mail Extensions) — стандартная строка вида тип/подтип,
     * которая описывает формат данных.
     * Клиент и сервер обмениваются меткой, чтобы понять,
     * что именно лежит в теле запроса/ответа и как его обрабатывать.
     */
    public static final String mimeType = "image/png";

    /**
     * Минимальный валидный PNG 1x1 пиксель (70 байт), прозрачный RGBA.
     * Закодирован в Base64, чтобы хранить бинарный файл прямо в коде
     * без внешних ресурсов. Проходит серверную валидацию изображения.
     */
    public static final byte[] PNG_1X1 = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    /**
     * Уникальное имя файла для загрузки: "qa_photo_" + UUID + ".png",
     * чтобы повторные загрузки не конфликтовали по имени.
     */
    public static String uniqueName() {
        return "qa_photo_" + UUID.randomUUID() + ".png";
    }

    /** Случайный UUID — для проверки «фото по несуществующему id». */
    public static String uniqueId() {
        return UUID.randomUUID().toString();
    }
}
