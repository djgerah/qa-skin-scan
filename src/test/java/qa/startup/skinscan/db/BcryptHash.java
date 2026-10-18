package qa.startup.skinscan.db;


import java.util.regex.Pattern;

public class BcryptHash {
    /**
     * BCrypt — это алгоритм хеширования паролей.
     * Результат — строка фиксированной длины 60 символов с внутренней структурой:
     * Шаблон BCrypt-хеша (колонка users.password):
     * $2a$12$RgtJlVaWeeP6KkTxPGJgIehncHWzSBZYiI91BW11YuJcaiDd55WMm
     * └┬─┘└┬┘└┬┘└───────────────────────┬──────────────────────┘
     *  │   │  │                        соль + хеш, 53 символа
     *  │   │  cost (2^12 = 4096 итераций)
     *  │   версия алгоритма: 2a / 2b / 2y
     *  маркер BCrypt
     */
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private BcryptHash() {
    }

    public static boolean isBcrypt(String hash) {
        return BCRYPT.matcher(hash).matches();
    }
}
