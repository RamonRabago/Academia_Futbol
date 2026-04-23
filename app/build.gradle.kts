import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

// Si no existe google-services.json (gitignore), copia el example para que compile; sustituye por el JSON real de Firebase Console.
run {
    val gsOut = layout.projectDirectory.file("google-services.json").asFile
    val gsIn = layout.projectDirectory.file("google-services.json.example").asFile
    if (!gsOut.exists() && gsIn.exists()) {
        gsIn.copyTo(gsOut, overwrite = true)
    }
}

val localSupabaseProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun escapeForBuildConfig(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"")

// Evita duplicate class (p. ej. UserInfo) cuando Gradle mezcla auth-kt/supabase-kt JVM con *-android.
val supabaseVersion = libs.versions.supabase.get()
configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("io.github.jan-tennert.supabase:auth-kt"))
            .using(module("io.github.jan-tennert.supabase:auth-kt-android:$supabaseVersion"))
            .because("Android: una sola variante Auth en el classpath")
        substitute(module("io.github.jan-tennert.supabase:supabase-kt"))
            .using(module("io.github.jan-tennert.supabase:supabase-kt-android:$supabaseVersion"))
            .because("Android: una sola variante core Supabase en el classpath")
        substitute(module("io.github.jan-tennert.supabase:storage-kt"))
            .using(module("io.github.jan-tennert.supabase:storage-kt-android:$supabaseVersion"))
            .because("Android: una sola variante Storage en el classpath")
    }
}

android {
    namespace = "com.escuelafutbol.academia"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.escuelafutbol.academia"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.2"
        val supabaseUrl = localSupabaseProps.getProperty("SUPABASE_URL", "").trim()
        val supabaseKey = localSupabaseProps.getProperty("SUPABASE_ANON_KEY", "").trim()
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${escapeForBuildConfig(supabaseUrl)}\"",
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${escapeForBuildConfig(supabaseKey)}\"",
        )
    }

    buildTypes {
        release {
            /** Firma con el keystore *debug* del SDK: APK instalable por WhatsApp sin Play Store (no usar para publicación en tienda). */
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(project(":ucrop"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth.android)
    implementation(libs.supabase.postgrest.android)
    implementation(libs.supabase.storage.android)
    implementation(libs.ktor.client.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    testImplementation("junit:junit:4.13.2")
}
