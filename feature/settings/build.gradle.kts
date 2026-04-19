plugins {
    id("alarmapp.android.feature")
}

android {
    namespace = "com.alarmapp.feature.settings"
}

dependencies {
    implementation(project(":core:data"))
}
