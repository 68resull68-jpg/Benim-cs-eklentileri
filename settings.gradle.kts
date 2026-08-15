pluginManagement {
    repositories {
        maven("https://repo.lagradost.cloud/releases")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "Cloudstream-Repo"
include(":app")
include(":DizipalProvider")
