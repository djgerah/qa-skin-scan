package qa.startup.skinscan.db;

import java.util.regex.Pattern;

/**
 * Формат BCrypt-хеша (колонка users.password).
 */
public final class BcryptHash {
    /**
     * BCrypt — алгоритм хеширования паролей. Результат — строка из 60 символов:
     * $2a$12$RgtJlVaWeeP6KkTxPGJgIehncHWzSBZYiI91BW11YuJcaiDd55WMm
     * └──┬──┘└──────────┬─────────┘└──────────────┬──────────────┘
     * маркер + cost соль, 22 хеш,             31 символ
     * Соль — случайные данные, добавляемые к паролю перед хешированием и
     * хранимые открыто внутри хеша. Из-за неё один и тот же пароль даёт разные хеши,
     * что защищает от радужных таблиц и не позволяет сравнить пароли пользователей
     * между собой.
     * Варианты формата [aby]: $2a$ — большинство реализаций,
     * включая Spring; $2b$ — OpenBSD; $2y$ — PHP. Принимаются все три,
     * так как проверяется, что это BCrypt-хеш, а не конкретная его реализация.
     * Cost — сложность, записанная двумя десятичными цифрами (здесь 12):
     * число итераций = 2^cost, то есть 2^12 = 4096.
     * В шаблоне эти две цифры ловит d{2} — «ровно две цифры».
     */
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private BcryptHash() {
    }

    /**
     * Возвращает true, если значение имеет структуру BCrypt-хеша.
     */
    public static boolean isBcrypt(String hash) {
        return hash != null && BCRYPT.matcher(hash).matches();
    }
}
