plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "no.synth.revertalicious"
        minSdkVersion(29)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("plugin.properties")
    }
    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree("libs") { include(listOf("*.jar")) })
    implementation(platform("com.google.firebase:firebase-bom:26.7.0"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.31")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.3.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:5.10.0.202012080955-r")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.3.0")
    testImplementation("org.mockito:mockito-core:3.8.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}
apply(plugin = "com.google.gms.google-services")
