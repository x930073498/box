plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.application.data1.payment"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.application.data1.payment"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("com.github.Justson.AgentWeb:agentweb-core:v5.0.6-androidx") // (必选)
    implementation("com.github.Justson.AgentWeb:agentweb-filechooser:v5.0.6-androidx") // (可选)
    implementation("com.github.Justson:Downloader:v5.0.4-androidx")

    api(project(":box"))
    implementation(project(":android-box"))
    implementation("androidx.core:core-ktx:1.12.0")
}