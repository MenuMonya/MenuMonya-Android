import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

// keystore 파일 조회
var keystoreProperties: Properties = Properties()
val keystoreFile = rootProject.file("keystore/keystore.properties")
if (keystoreFile.exists()) {
    keystoreProperties = Properties().apply {
        load(FileInputStream(keystoreFile))
    }
} else {
    throw FileNotFoundException()
}

val localProperties = gradleLocalProperties(rootDir)

android {
    namespace = "com.woozoo.menumonya"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.woozoo.menumonya"
        minSdk = 24
        targetSdk = 33
        versionCode = 19
        versionName = "0.1.0-hotfix"

        testInstrumentationRunner = "com.woozoo.menumonya.HiltTestRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties.getProperty("appKeyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            resValue("string", "NAVER_CLIENT_ID", localProperties.getProperty("NAVER_CLIENT_ID"))
            manifestPlaceholders.put("gaEnabled", "false")
            manifestPlaceholders["app_name"] = "@string/app_name_debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            resValue("string", "NAVER_CLIENT_ID", localProperties.getProperty("NAVER_CLIENT_ID"))
            manifestPlaceholders.put("gaEnabled", "true")
            manifestPlaceholders["app_name"] = "@string/app_name"
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_9
        targetCompatibility = JavaVersion.VERSION_1_9
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    // UI Test(Espresso)
    configurations.forEach {
        it.exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    testOptions {
        animationsDisabled = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(
        fileTree(
            mapOf(
                "dir" to "src/main/jniLibs",
                "include" to listOf("*.arr", "*jar")
            )
        )
    )
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")

    // 지도 관련 라이브러리
    implementation("com.naver.maps:map-sdk:3.17.0") {
        exclude(group = "com.android.support")
    }
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:31.3.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")

    // Lottie
    implementation("com.airbnb.android:lottie:6.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Test
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    androidTestImplementation("org.mockito:mockito-android:2.24.5")
    // androidTest에서 mockito 사용할 경우 필요함.

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    testImplementation("com.google.truth:truth:1.1.4")
    androidTestImplementation("com.google.truth:truth:1.1.4")

    testImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:runner:1.5.2")

    // Hilt testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}