import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val androidExtension = extensions.findByName("android") as? CommonExtension<*, *, *, *, *, *>
                ?: error("Apply com.android.application or com.android.library before alarmapp.android.compose")

            androidExtension.apply {
                buildFeatures.compose = true
            }

            dependencies {
                val bom = libs.findLibrary("androidx-compose-bom").get()
                add("implementation", platform(bom))
                add("androidTestImplementation", platform(bom))

                listOf(
                    "androidx-compose-ui",
                    "androidx-compose-ui-graphics",
                    "androidx-compose-ui-tooling-preview",
                    "androidx-compose-material3",
                    "androidx-compose-material-icons-extended",
                    "androidx-compose-foundation",
                    "androidx-activity-compose",
                    "androidx-lifecycle-runtime-compose",
                ).forEach { alias ->
                    add("implementation", libs.findLibrary(alias).get())
                }

                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
                add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
            }
        }
    }
}
