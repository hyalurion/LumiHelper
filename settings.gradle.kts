pluginManagement {
    repositories {
        // 阿里云镜像（推荐用于国内）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

        // 原始仓库（如果镜像不可用）
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
        // 阿里云镜像（推荐用于国内）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }

        // 原始仓库（如果镜像不可用）
        google()
        mavenCentral()
    }
}

rootProject.name = "HelperUnited"
include(":app")
include(":core:base")
include(":core:model")
include(":core:network")
include(":core:data")
