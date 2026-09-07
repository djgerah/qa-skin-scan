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
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    testRuntimeOnly("org.postgresql:postgresql:42.7.11")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform {
        System.getProperty("tags")?.let {
            includeTags(*it.split(",").map(String::trim).toTypedArray())
        }
    }

    testLogging {
        events("passed", "failed", "skipped")
        showExceptions = true
    }
}