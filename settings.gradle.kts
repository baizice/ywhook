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
        // YukiHook 备用仓库（Sonatype 发布失败时兜底）
        maven("https://raw.githubusercontent.com/HighCapable/maven-repository/main/repository/releases")
        // Xposed Framework API（yukihook-runtime-xposed82 依赖）
        maven("https://api.xposed.info/") {
            content { includeGroup("de.robv.android.xposed") }
        }
    }
}
rootProject.name = "ReaMicroVipHookYuki"
include(":app")