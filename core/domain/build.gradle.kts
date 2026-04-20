plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

// Use the JVM toolchain to force every JVM tool in this module (javac AND
// kotlinc) onto JDK 17, regardless of the JDK running the Gradle daemon.
// Without this, a daemon on JDK 21 produces compileKotlin=21 / compileJava=17
// which Kotlin 2.0+ rejects with "Inconsistent JVM-target compatibility".
kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
