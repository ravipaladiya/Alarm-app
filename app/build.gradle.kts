import java.util.Properties

plugins {
    id("alarmapp.android.application")
    id("alarmapp.android.compose")
    id("alarmapp.android.hilt")
    // Declared but not applied here — we only apply it conditionally below so
    // contributor builds without a Firebase project still compile.
    alias(libs.plugins.google.services) apply false
}

val googleServicesJson: java.io.File = rootProject.file("app/google-services.json")
val hasGoogleServices = googleServicesJson.exists()
if (hasGoogleServices) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

// Read keystore credentials from a git-ignored keystore.properties file (see
// keystore.properties.template). Release builds fall back to the debug keystore
// if the file is missing, so development flows don't break for contributors
// without the release key, but CI will fail the sign step — by design.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}
val hasReleaseKeystore = keystoreProperties.getProperty("storeFile") != null

android {
    namespace = "com.alarmapp"

    defaultConfig {
        applicationId = "com.alarmapp"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "com.alarmapp.HiltTestRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                // Developer-convenience fallback: dev can build `:app:assembleRelease`
                // locally without a release key. Play Store / CI must supply a real
                // keystore.properties.
                signingConfigs.getByName("debug")
            }
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
            )
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:alarm"))
    implementation(project(":feature:alarms"))
    implementation(project(":feature:ringing"))
    implementation(project(":feature:challenges"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:sleep"))
    implementation(project(":feature:permissions"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
