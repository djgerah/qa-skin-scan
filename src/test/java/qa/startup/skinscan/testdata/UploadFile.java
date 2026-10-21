package qa.startup.skinscan.testdata;

import java.util.UUID;

/**
 * Базовый загружаемый файл: имя, MIME-тип и содержимое.
 * Наследники в своём конструкторе задают имя через {@link #uniqueName()},
 * а также MIME-тип и содержимое.
 */
public abstract class UploadFile {

    /**
     * Имя файла в multipart-запросе. Уникально, чтобы повторные
     * загрузки не конфликтовали по имени.
     */
    public String name;

    /**
     * Случайный UUID — заведомо несуществующий id на сервере.
     */
    public final String id;

    /**
     * MIME-тип (Multipurpose Internet Mail Extensions) — стандартная строка вида тип/подтип,
     * которая описывает формат данных. Клиент и сервер обмениваются меткой, чтобы понять,
     * что именно лежит в теле запроса/ответа и как его обрабатывать.
     */
    public String mimeType = "";

    /**
     * Байты файла — то, что уходит в теле multipart-запроса.
     */
    public byte[] CONTENT = new byte[0];

    protected UploadFile() {
        this.id = uniqueId();
    }

    /**
     * Уникальное имя файла с расширением, зависящим от типа файла.
     */
    protected abstract String uniqueName();

    /**
     * Уникальный UUID.
     */
    private static String uniqueId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Задаёт имя файла вручную.
     */
    public void setName(String name) {
        this.name = name;
    }
}
