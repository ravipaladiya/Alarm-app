plugins {
    id("alarmapp.android.library")
    id("alarmapp.android.hilt")
    id("alarmapp.android.room")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.alarmapp.core.data"
    // Expose the exported Room schemas as androidTest assets so
    // MigrationTestHelper can load the previous schema JSON at runtime.
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
}

// Export Room schemas to source control so every version bump is reviewable
// and migrations can be verified against a known-good previous schema.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
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

    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
