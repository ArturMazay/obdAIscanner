@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import java.util.Properties

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.hotReload)
    alias(libs.plugins.kotlinx.serialization)
    // Room плагин отключен для iOS - вызывает проблемы сборки
    // alias(libs.plugins.room)
    // alias(libs.plugins.ksp)

}

kotlin {
    jvmToolchain(11)
    androidTarget {
        //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm()

    wasmJs {
        browser()
        binaries.executable()
    }

    // iOS таргеты - явное объявление для лучшей поддержки IDE
    val iosTargets = listOf(
        iosX64("iosX64"),
        iosArm64("iosArm64"),
        iosSimulatorArm64("iosSimulatorArm64")
    )
    
    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.serialization)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.coil)
            implementation(libs.coil.network.ktor)
            implementation(libs.koin.compose.viewmodel.nav)
            implementation(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            // Lifecycle зависимости только для Android
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

    }

}

android {
    namespace = "com.aistudio"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 35

        applicationId = "com.aistudio.androidApp"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Читаем токен из local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        val hfToken = localProperties.getProperty("hf.api.token") ?: System.getenv("HF_API_TOKEN") ?: ""
        
        val tokenPreview = if (hfToken.length > 10) "${hfToken.take(10)}..." else hfToken
        println("🔵 [build.gradle.kts] Токен из local.properties: $tokenPreview")
        println("🔵 [build.gradle.kts] Длина токена: ${hfToken.length}")
        if (hfToken.isEmpty() || hfToken == "YOUR_HUGGING_FACE_TOKEN_HERE") {
            println("🔴 [build.gradle.kts] ВНИМАНИЕ: Токен не установлен в local.properties!")
        }
        
        buildConfigField("String", "HF_API_TOKEN", "\"$hfToken\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}

//https://developer.android.com/develop/ui/compose/testing#setup
dependencies {
    androidTestImplementation(libs.androidx.uitest.junit4)
    debugImplementation(libs.androidx.uitest.testManifest)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OBDAI"
            packageVersion = "1.0.0"

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = "com.aistudio.desktopApp"
            }
        }
    }
}

//https://github.com/JetBrains/compose-hot-reload
composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}
//tasks.withType<ComposeHotRun>().configureEach {
//    mainClass.set("MainKt")
//}

// Room конфигурация отключена - не используется в проекте
// room {
//     schemaDirectory("$projectDir/schemas")
// }

// KSP зависимости отключены - Room не используется
// dependencies {
//     with(libs.room.compiler) {
//         add("kspAndroid", this)
//         add("kspJvm", this)
//         add("kspIosX64", this)
//         add("kspIosArm64", this)
//         add("kspIosSimulatorArm64", this)
//     }
// }
