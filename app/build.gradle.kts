plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.penny"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.penny"
        minSdk = 33
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX and Material dependencies
    implementation(libs.appcompat)
    implementation(libs.material)

    // Firebase and Google Sign-In dependencies
    implementation(platform(libs.firebase.bom)) // Firebase BOM for version management
    implementation(libs.firebase.auth)              // Firebase Authentication
    implementation(libs.play.services.auth)
    implementation(libs.activity)
    implementation(libs.constraintlayout)         // Google Sign-In
    implementation(libs.glide)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.pie.chart)



    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
