plugins {
    id("alarmapp.android.library")
    id("alarmapp.android.compose")
}

android {
    namespace = "com.alarmapp.core.designsystem"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
}
