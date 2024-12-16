import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.project"
    compileSdk = 35

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    } else {
        throw GradleException("local.properties 파일을 찾을 수 없습니다!")
    }

    defaultConfig {
        applicationId = "com.example.project"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties에서 API 정보 가져오기
        buildConfigField("String", "NAVER_OCR_API_URL", "\"${localProperties["NAVER_OCR_API_URL"]}\"")
        buildConfigField("String", "NAVER_OCR_API_KEY", "\"${localProperties["NAVER_OCR_API_KEY"]}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Glide 추가
    implementation(libs.glide) // Glide 추가
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1") // Glide Compiler 추가

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

// Retrofit - Gson 컨버터 (JSON 변환용)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp (Retrofit 기본 네트워크 라이브러리)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

// OkHttp 로깅 인터셉터 (디버깅용)
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    //health 커넥트
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")

    implementation("com.google.code.gson:gson:2.10")
}