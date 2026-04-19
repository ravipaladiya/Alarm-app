import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

allprojects {
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
    configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktlint(rootProject.libs.versions.ktlint.get())
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_filename" to "disabled",
                        "ktlint_standard_function-signature" to "disabled",
                    ),
                )
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(rootProject.libs.versions.ktlint.get())
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/baseline.xml")
    parallel = true
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)
    }
}
