plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.chandan.askfaculty"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.chandan.askfaculty"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val debugBaseUrl = project.findProperty("askfaculty.debug.baseUrl") as? String ?: "http://10.0.2.2:5000/api/v1"
            buildConfigField("String", "API_BASE_URL", "\"$debugBaseUrl\"")
        }
        release {
            val releaseBaseUrl = project.findProperty("askfaculty.release.baseUrl") as? String ?: ""
            buildConfigField("String", "API_BASE_URL", "\"$releaseBaseUrl\"")
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
