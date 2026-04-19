plugins {
    id("alarmapp.android.feature")
}

android {
    namespace = "com.alarmapp.feature.alarms"
}

dependencies {
    implementation(project(":feature:permissions"))

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
