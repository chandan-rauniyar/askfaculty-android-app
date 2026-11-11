plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.chandan.askfaculty"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chandan.askfaculty"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val debugBaseUrl = project.findProperty("askfaculty.debug.baseUrl") as? String ?: "http://10.0.2.2:5000"
            buildConfigField("String", "API_BASE_URL", "\"$debugBaseUrl\"")
        }
        release {
            val releaseBaseUrl = project.findProperty("askfaculty.release.baseUrl") as? String ?: ""
            buildConfigField("String", "API_BASE_URL", "\"$releaseBaseUrl\"")
            isMinifyEnabled = false
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
        compose = true
        buildConfig = true
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { _ ->
        val taskNames = gradle.startParameter.taskNames
        val isBuildingRelease = taskNames.any { it.contains("Release", ignoreCase = true) } ||
            taskNames.any { it == "assemble" || it == "build" }
        if (isBuildingRelease) {
            val releaseBaseUrl = project.findProperty("askfaculty.release.baseUrl") as? String
            if (releaseBaseUrl.isNullOrBlank()) {
                throw GradleException("Release build requires 'askfaculty.release.baseUrl' to be set in gradle.properties or local.properties.")
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.security.crypto)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
