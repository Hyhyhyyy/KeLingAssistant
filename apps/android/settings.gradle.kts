// 插件管理配置，指定从哪里下载Gradle插件
pluginManagement {
    repositories {
        // Google的仓库，Android专用库
        google()
        // Maven中央仓库，大多数开源库
        mavenCentral()
        // Gradle插件仓库
        gradlePluginPortal()
    }
}

// 依赖解析配置，指定从哪里下载App依赖
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// 项目根目录名称
rootProject.name = "KeLing"

// 包含的模块，目前只有app模块
include(":app")