plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.op.hook.reamicro"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.op.hook.reamicro"
        minSdk = 26
        targetSdk = 36
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
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources.excludes += setOf("META-INF/*")
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    implementation(platform("com.highcapable.yukihookapi:yukihook-bom:1.5.0"))
    implementation("com.highcapable.yukihookapi:yukihook-core")
    implementation("com.highcapable.yukihookapi:yukihook-runtime-xposed82")
    implementation("com.highcapable.yukihookapi:yukihook-extension-kavaref")
    implementation(platform("com.highcapable.kavaref:kavaref-bom:1.1.1"))
    implementation("com.highcapable.kavaref:kavaref-core")
    implementation("com.highcapable.kavaref:kavaref-android")
    implementation("com.highcapable.kavaref:kavaref-extension")
    ksp("com.highcapable.yukihookapi:yukihook-compiler")
}