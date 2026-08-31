plugins {
    id("java")
}

group = "qa.startup.skinscan"
version = "0.0.1-SNAPSHOT"
description = "QA automation для SkinScan API (black-box, REST Assured + JDBC)"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.awaitility:awaitility:4.2.1")
    // Jackson для сериализации моделей запросов (UserRequest, PasswordUpdateRequest)
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    // JDBC-драйвер для DB-верификации (DbVerificationTest)
    testRuntimeOnly("org.postgresql:postgresql:42.7.11")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    // Адрес тестируемого сервиса: -DbBaseUrl=... или переменная окружения BASE_URL
    val testBaseUrl = System.getProperty("baseUrl")
        ?: System.getenv("BASE_URL")
        ?: "http://localhost:8080"
    systemProperty("baseUrl", testBaseUrl)

    // Параметры БД для DbVerificationTest: -Ddb.url / -Ddb.user / -Ddb.password
    // По умолчанию читаются из src/test/resources/connectionBD.properties

    useJUnitPlatform {
        // Фильтрация по тегам: ./gradlew test -Dtags=smoke
        val tags = System.getProperty("tags")
        if (tags != null) {
            includeTags(*tags.split(",").map { it.trim() }.toTypedArray())
        }
    }

    testLogging {
        events("passed", "failed", "skipped")
        showExceptions = true
    }
}
