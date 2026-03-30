import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.*

allprojects {
    group = "ru.sg"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// ========================================
// CENTRALIZED VERSION MANAGEMENT
// ========================================
ext {
    set("springBootVersion", "4.0.3")
    set("springCloudVersion", "2025.1.1")
    set("kotlinVersion", "2.2.21")
    set("junitVersion", "5.10.0")
    set("lombokVersion", "1.18.30")
    set("postgresqlVersion", "42.7.3")
    set("jacksonVersion", "2.17.1")
    set("keycloakVersion", "26.0.8")
    set("resilience4jVersion", "2.1.0")
    set("springdocOpenApiVersion", "2.6.0")
    set("micrometer", "1.13.1")
}

subprojects {
    apply(plugin = "java")

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:${rootProject.ext["junitVersion"]}")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}