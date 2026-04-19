plugins {
    id("alarmapp.android.library")
    id("alarmapp.android.hilt")
}

android {
    namespace = "com.alarmapp.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
