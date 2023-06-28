plugins {
    kotlin("jvm") version "1.8.21"
}

group = "br.pucpr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val version = "5.3.0"
    testImplementation(kotlin("test"))
    testImplementation("io.rest-assured:rest-assured:$version")
    testImplementation("io.rest-assured:json-schema-validator:$version")
    testImplementation("io.rest-assured:kotlin-extensions:$version")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}