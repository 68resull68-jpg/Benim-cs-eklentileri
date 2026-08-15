plugins {
    kotlin("jvm") version "2.0.20"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.lagradost.cloud/releases")
}

dependencies {
    compileOnly("com.lagradost:cloudstream:1.0")
}

group = "com.lagradost.cloudstream3.dizipal"
version = "1.0.0"
