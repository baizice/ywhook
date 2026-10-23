pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // YukiHookAPI 依赖
        maven("https://api.xposed.info/")
    }
}
rootProject.name = "ReaMicroVipHookYuki"
include(":app")