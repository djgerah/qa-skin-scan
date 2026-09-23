package qa.startup.skinscan.testdata;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Тестовые данные для загрузки документов.
 */
public final class Document {

    /**
     * MIME-тип (Multipurpose Internet Mail Extensions) — стандартная строка вида тип/подтип,
     * которая описывает формат данных.
     * Клиент и сервер обмениваются меткой, чтобы понять,
     * что именно лежит в теле запроса/ответа и как его обрабатывать.
     */
    public static final String mimeType = "text/plain";

    /**
     * Произвольное текстовое содержимое.
     */
    public static final byte[] TXT_CONTENT =
            "Текстовый документ".getBytes(StandardCharsets.UTF_8);

    /**
     * Уникальное имя документа: "qa_document_" + UUID + ".txt".
     */
    public static String uniqueName() {
        return "qa_document_" + UUID.randomUUID() + ".txt";
    }
}
