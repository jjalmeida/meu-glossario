import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting
        val wasmJsMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.navigation.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.kotlinx.coroutines.play.services)
            implementation(libs.google.api.client.android)
            implementation(libs.google.api.drive)
            implementation(libs.play.services.auth)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.google.api.client)
            implementation(libs.google.api.drive)
            implementation(libs.google.oauth.client.jetty)
        }

        desktopTest.dependencies {
            implementation(kotlin("test-junit"))
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

// Permite redirecionar a pasta de build para fora de diretórios sincronizados
// (ex.: OneDrive), evitando travas de arquivo durante o build:
//   ./gradlew -PbuildOutDir=C:/tmp/mg-build :composeApp:desktopTest
(findProperty("buildOutDir") as String?)?.let { out ->
    layout.buildDirectory.set(file("$out/composeApp"))
}

// Carrega as credenciais de assinatura de keystore/keystore.properties (fora do VCS).
val keystorePropertiesFile = rootProject.file("keystore/keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.meuglossario"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.meuglossario"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                // storeFile em keystore.properties é relativo à raiz do projeto.
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Pacote determinístico para a classe de recursos gerada (Res).
compose.resources {
    publicResClass = true
    packageOfResClass = "com.meuglossario.resources"
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "com.meuglossario.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MeuGlossario"
            packageVersion = "1.0.0"
        }
    }
}

// Garante que a task de teste do target desktop use JUnit e enxergue as
// classes compiladas de commonTest (classes/kotlin/desktop/test).
tasks.named<Test>("desktopTest") {
    useJUnit()
    val testClasses = layout.buildDirectory.dir("classes/kotlin/desktop/test")
    testClassesDirs = files(testClasses) + testClassesDirs
    classpath = files(testClasses) + classpath
}
