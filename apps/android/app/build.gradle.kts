/**
 * 这是应用模块的配置，控制我们App的所有功能
 * 每一行都有详细注释，请仔细阅读
 */

plugins {
    // 应用Android应用插件，这样Gradle知道如何构建APK
    id("com.android.application")
    // 应用Kotlin插件，支持Kotlin语言
    id("org.jetbrains.kotlin.android")
    // Kotlin序列化插件，用于AI请求的JSON转换
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    // 应用的命名空间，必须是唯一的，通常用反向域名
    namespace = "com.keling.app"

    // 编译SDK版本，34是最新的Android 14
    compileSdk = 34

    // 签名配置
    signingConfigs {
        create("release") {
            storeFile = file("../keling-release.jks")
            storePassword = "keling123"
            keyAlias = "keling"
            keyPassword = "keling123"
        }
    }

    defaultConfig {
        // 应用的唯一标识，发布到应用商店后不能改
        applicationId = "com.keling.app"

        // 最低支持的Android版本，API 26 = Android 8.0
        // 这样大约90%的设备都能运行
        minSdk = 26

        // 目标SDK版本，表示为这个版本优化
        targetSdk = 34

        // 版本号，每次更新应用要+1
        versionCode = 3
        // 版本名称，用户看到的版本
        versionName = "3.0.0"

        // 测试运行器，用于自动化测试（暂时不用管）
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 启用矢量图支持，这样图标可以缩放不失真
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "DEEPSEEK_API_KEY",
            "\"sk-374b9fb3344e4b32be81023272bd162f\""
        )

        buildFeatures { buildConfig = true }
    }



    /**
     * 构建类型配置
     * debug: 开发时使用，包含调试信息，文件大
     * release: 发布时使用，优化代码，文件小
     */
    buildTypes {
        release {
            // 使用release签名配置
            signingConfig = signingConfigs.getByName("release")
            // 是否启用代码压缩，release版本一定要true
            isMinifyEnabled = false
            // 代码混淆规则文件（暂时不用）
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java编译配置，使用Java 17的新特性
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Kotlin编译配置，与Java版本对应
    kotlinOptions {
        jvmTarget = "17"
    }

    // 启用Compose功能，这是现代Android UI框架
    buildFeatures {
        compose = true
    }

    // Compose编译器版本，必须与Kotlin版本配套
    // Kotlin 1.9.22 对应 Compose Compiler 1.5.8
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    // 打包配置，排除一些不需要的文件
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // 把 Cursor 工作区里提供的星球图片打包进 APK 的 assets 目录
    // 代码中会用 `file:///android_asset/<文件名>` 来加载这些图片
    sourceSets {
        getByName("main") {
            assets.srcDir("../../../.cursor/projects/c-Users-13581-Desktop-KeLing3-0/assets")
        }
    }
}

/**
 * 依赖配置：这里列出App需要的所有第三方库
 * 每个库都有特定用途，我按功能分组
 */
dependencies {
    // ==================== 核心Compose库 ====================
    // Compose物料清单（BOM），统一管理所有Compose库的版本
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI核心，包含所有基础组件（Text, Button, Column等）
    implementation("androidx.compose.ui:ui")
    // Compose图形库，用于自定义绘制
    implementation("androidx.compose.ui:ui-graphics")
    // Compose预览工具，Android Studio右侧可以实时预览
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Compose Material 3，Google的设计系统组件（按钮、卡片等）
    implementation("androidx.compose.material3:material3")
    // Compose动画库，用于实现平滑过渡效果
    implementation("androidx.compose.animation:animation")

    // ==================== 架构组件 ====================
    // ViewModel，用于管理界面数据和业务逻辑
    // 屏幕旋转时数据不会丢失
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    // 生命周期感知，自动管理资源释放
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // ==================== 导航 ====================
    // Compose导航，实现页面跳转
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ==================== 数据存储 ====================
    // DataStore，用于保存用户偏好设置（比SharedPreferences更现代）
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ==================== 后台任务 ====================
    // WorkManager，用于定时通知和后台任务
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ==================== 网络请求 ====================
    // Ktor客户端，用于调用AI API（比Retrofit更轻量，Kotlin原生）
    implementation("io.ktor:ktor-client-android:2.3.7")
    // JSON序列化支持，自动将JSON转为Kotlin对象
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Kotlin序列化插件，用于数据类
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ==================== 图片加载 ====================
    // Coil，用于加载网络图片（头像等），Compose专用
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ==================== 协程 ====================
    // Kotlin协程，用于异步编程（网络请求不卡界面）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ==================== 测试（暂时不用） ====================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // 调试工具，预览和检查布局
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}