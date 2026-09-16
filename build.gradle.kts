plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jooq.jooq-codegen-gradle") version "3.21.8"
}

group = "com.sol2one"
version = "0.0.1-SNAPSHOT"
description = "tree"

// Keep the jOOQ runtime, the code generator, and jooq-meta-extensions on the same version.
val jooqVersion = "3.21.8"
extra["jooq.version"] = jooqVersion

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
    implementation("tools.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.postgresql:postgresql")

    // Generate jOOQ classes from the Flyway migration files; no database needed at build time.
    jooqCodegen("org.jooq:jooq-meta-extensions:$jooqVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

jooq {
    configuration {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    property { key = "scripts"; value = "src/main/resources/db/migration" }
                    property { key = "sort"; value = "flyway" }
                    property { key = "defaultNameCase"; value = "lower" }
                }
            }
            generate {
                isDaos = false
                isPojos = false
                isRecords = true
            }
            target {
                packageName = "com.sol2one.jooq"
                directory = layout.buildDirectory
                    .dir("generated-src/jooq")
                    .get()
                    .asFile
                    .absolutePath
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Run jOOQ code generation before compiling
tasks.named("compileKotlin") {
    dependsOn(tasks.named("jooqCodegen"))
}