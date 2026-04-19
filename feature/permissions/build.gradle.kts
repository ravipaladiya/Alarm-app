plugins {
    id("alarmapp.android.feature")
}

android {
    namespace = "com.alarmapp.feature.permissions"
}

dependencies {
    implementation(libs.accompanist.permissions)
}
