plugins {
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"

    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
}

group = "ru.sg"
version = "0.0.1-SNAPSHOT"
description = "Config Server for main service"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

extra["springCloudVersion"] = "2025.1.1"

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springframework.cloud:spring-cloud-config-server")

    // optional
    // implementation("org.springframework.cloud:spring-cloud-starter-bus-kafka")
    // implementation("org.springframework.cloud:spring-cloud-config-monitor")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property"
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin")
    }
    test {
        kotlin.srcDirs("src/test/kotlin")
    }
}