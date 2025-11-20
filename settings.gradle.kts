pluginManagement {
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

rootProject.name = "AvitoTest"
include(":app")

include(":core:ui")
include(":core:common")
include(":core:database")
include(":core:firebase")
include(":core:navigation")
include(":core:di")
include(":feature:auth")
include(":feature:profile")
include(":feature:booksList")
include(":feature:bookReader")

include(":feature:bookUpload")
include(":core:di")
