plugins {
    id("alarmapp.android.library")
    id("alarmapp.android.hilt")
    id("alarmapp.android.room")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.alarmapp.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
}
