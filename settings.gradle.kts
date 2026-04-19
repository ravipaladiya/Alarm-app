pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AlarmApp"

include(":app")

include(":core:common")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:alarm")

include(":feature:alarms")
include(":feature:ringing")
include(":feature:challenges")
include(":feature:settings")
include(":feature:sleep")
