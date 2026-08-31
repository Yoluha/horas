plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.lucas.horas"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lucas.horas"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions += "tier"
    productFlavors {
        create("free") {
            dimension = "tier"
            // Tem anúncios (AdMob) — é a versão distribuída de graça.
        }
        create("pro") {
            dimension = "tier"
            // Sem anúncios, sem SDK de anúncios sequer incluído — é a versão vendida.
            versionNameSuffix = "-pro"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    "freeImplementation"("com.google.android.gms:play-services-ads:25.4.0")
    "freeImplementation"("com.google.android.ump:user-messaging-platform:4.0.0")
}
