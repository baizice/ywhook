plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.op.hook.reamicro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.op.hook.reamicro"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // 模块本体不需要混淆/压缩
    packaging {
        resources.excludes += setOf("META-INF/*")
    }
}

dependencies {
    // Xposed API 只用于编译期
    compileOnly("de.robv.android.xposed:api:82")

    // YukiHookAPI 核心
    implementation("com.highcapable.yukihookapi:api:1.1.11")
}