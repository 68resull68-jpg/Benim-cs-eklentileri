rootProject.name = "Cloudstream-Repo"
include(":app")
include(":DizipalProvider")

pluginManagement {
    repositories {
        maven("https://repo.lagradost.cloud/releases")
        gradlePluginPortal()
        mavenCentral()
    }
}
