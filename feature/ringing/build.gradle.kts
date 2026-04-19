plugins {
    id("alarmapp.android.feature")
}

android {
    namespace = "com.alarmapp.feature.ringing"
}

dependencies {
    implementation(project(":core:alarm"))
    implementation(project(":feature:challenges"))
}
