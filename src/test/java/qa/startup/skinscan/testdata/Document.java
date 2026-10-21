package qa.startup.skinscan.testdata;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Тестовые данные для загрузки документов.
 * Отделены от PhotoClient, чтобы клиент занимался только HTTP-вызовами.
 */
public final class Document extends UploadFile {

    public Document() {
        name = uniqueName();
        /*
         * MIME-тип (Multipurpose Internet Mail Extensions) — стандартная строка вида тип/подтип,
         * которая описывает формат данных. Клиент и сервер обмениваются меткой, чтобы понять,
         * что именно лежит в теле запроса/ответа и как его обрабатывать.
         */
        mimeType = "text/plain";
        /*
         * Произвольное текстовое содержимое в кодировке UTF-8.
         */
        CONTENT = "Текстовый документ".getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Уникальное имя документа: "qa_document_" + UUID + ".txt",
     * чтобы повторные загрузки не конфликтовали по имени.
     */
    @Override
    protected String uniqueName() {
        return "qa_document_" + UUID.randomUUID() + ".txt";
    }
}
