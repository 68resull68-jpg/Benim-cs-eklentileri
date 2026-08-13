dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

version = 1

cloudstream {
    description = "AniList Türkçe anime arama eklentisi"
    authors = listOf("68resull68-jpg")
    status = 1
    tvTypes = listOf("Movie")
    requiresResources = false
    language = "tr"
    iconUrl = "https://upload.wikimedia.org/wikipedia/commons/2/2f/Korduene_Logo.png"
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
