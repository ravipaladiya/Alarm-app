plugins {
    id("alarmapp.android.feature")
}

android {
    namespace = "com.alarmapp.feature.alarms"
}

dependencies {
    implementation(project(":feature:permissions"))
}
