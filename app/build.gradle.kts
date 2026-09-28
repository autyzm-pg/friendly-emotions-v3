import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val isGithubActions = System.getenv("GITHUB_ACTIONS") == "true"

val keystoreProperties =
    Properties().apply {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            keystorePropertiesFile.inputStream().use { load(it) }
        }
    }

fun signingProp(key: String): String? = if (isGithubActions) System.getenv(key) else keystoreProperties.getProperty(key)

val majorCounter = System.getenv("MAJOR_COUNTER")?.toIntOrNull() ?: 1
val minorCounter = System.getenv("MINOR_COUNTER")?.toIntOrNull() ?: 0
val patchCounter = System.getenv("PATCH_COUNTER")?.toIntOrNull() ?: 0

android {
    namespace = "pg.autyzm.friendlyemotions"
    compileSdk = 36

    defaultConfig {
        applicationId = "pg.autyzm.friendlyemotions"
        minSdk = 24
        targetSdk = 36
        versionCode =
            if (isGithubActions) {
                System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
            } else {
                maxOf(patchCounter, 1)
            }
        versionName = "$majorCounter.$minorCounter.$patchCounter"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val path = signingProp("KEYSTORE_PATH")
            storeFile = path?.let { file(it) }
            storePassword = signingProp("KEYSTORE_PASSWORD")
            keyAlias = signingProp("KEY_ALIAS")
            keyPassword = signingProp("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature:child"))
    implementation(project(":feature:therapist"))
    implementation(project(":core:ui"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.android.testing)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
