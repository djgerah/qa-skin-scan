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
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation(platform("io.qameta.allure:allure-bom:2.29.0"))
    testImplementation("io.qameta.allure:allure-junit5")
    testRuntimeOnly("org.aspectj:aspectjweaver:1.9.22")

    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation("org.awaitility:awaitility:4.2.1")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")

    testRuntimeOnly("org.postgresql:postgresql:42.7.11")
}

tasks.test {
    useJUnitPlatform {
        System.getProperty("tags")?.let {
            includeTags(*it.split(",").map(String::trim).toTypedArray())
        }
    }

    listOf("base.url", "db.url", "db.user", "db.password").forEach { key ->
        System.getProperty(key)?.let { systemProperty(key, it) }
    }

    testLogging {
        events("passed", "failed", "skipped")
        showExceptions = true
    }

    val aspectjWeaver = configurations.testRuntimeClasspath.get()
        .incoming.files.files
        .first { it.name.startsWith("aspectjweaver-") }
    jvmArgs("-javaagent:$aspectjWeaver")
}

tasks.register<Exec>("allureReport") {
    doFirst {
        delete("build/allure-report")
    }

    commandLine("allure", "generate", "build/allure-results", "-o", "build/allure-report")
}

tasks.register<Exec>("allureServe") {
    commandLine("allure", "serve", "build/allure-results")
}