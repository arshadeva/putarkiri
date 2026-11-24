plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
val backendBaseUrl = (System.getenv("OCALIX_BACKEND_URL")
    ?: (project.findProperty("OCALIX_BACKEND_URL") as String?)
    ?: "").trim().trimEnd('/')
val escapedBackendUrl = backendBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.ocalix.music"
    compileSdk = 35
    buildFeatures { buildConfig = true }
    defaultConfig {
        applicationId = "com.ocalix.music"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
        buildConfigField("String", "BACKEND_BASE_URL", "\"$escapedBackendUrl\"")
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.webkit:webkit:1.12.1")
}
