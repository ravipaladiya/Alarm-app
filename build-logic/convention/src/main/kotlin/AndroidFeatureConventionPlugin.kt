import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("alarmapp.android.library")
            pluginManager.apply("alarmapp.android.hilt")
            pluginManager.apply("alarmapp.android.compose")

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:domain"))

                add("implementation", libs.findLibrary("androidx-core-ktx").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-ktx").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
                add("implementation", libs.findLibrary("timber").get())

                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("testImplementation", libs.findLibrary("turbine").get())
                add("testImplementation", libs.findLibrary("mockk").get())
            }
        }
    }
}
