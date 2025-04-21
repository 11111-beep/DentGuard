plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")  // 添加 KAPT 插件
}

android {
    namespace = "com.example.dentguard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dentguard"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("org.jsoup:jsoup:1.16.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.guolindev.permissionx:permissionx:1.7.1")
    implementation("androidx.room:room-runtime:2.6.1")  // Room 的运行时库
    kapt("androidx.room:room-compiler:2.6.1")  // Room 的编译器，使用 KAPT 注解处理
    implementation("androidx.room:room-ktx:2.6.1")  // Room 的扩展库，提供了一些扩展函数和属性.
    implementation ("androidx.work:work-runtime-ktx:2.8.1")
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}