import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.op.hook.reamicro"
    // YukiHook 1.5.0 的 AAR 要求 compileSdk >= 37
    compileSdk = 37

    defaultConfig {
        applicationId = "com.op.hook.reamicro"
        minSdk = 26
        targetSdk = 37
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

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf("META-INF/*")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Xposed Framework API（编译期）
    compileOnly("de.robv.android.xposed:api:82")

    // YukiHook 1.5.0 —— 核心 + xposed82 运行时 + KavaRef 扩展 + KSP 编译器
    implementation(platform("com.highcapable.yukihookapi:yukihook-bom:1.5.0"))
    implementation("com.highcapable.yukihookapi:yukihook-core")
    implementation("com.highcapable.yukihookapi:yukihook-runtime-xposed82")
    implementation("com.highcapable.yukihookapi:yukihook-extension-kavaref")
    implementation(platform("com.highcapable.kavaref:kavaref-bom:1.1.1"))
    implementation("com.highcapable.kavaref:kavaref-core")
    implementation("com.highcapable.kavaref:kavaref-android")
    implementation("com.highcapable.kavaref:kavaref-extension")

    // 生成模块入口 / xposed_init 等
    ksp("com.highcapable.yukihookapi:yukihook-compiler")
}