import java.io.FileInputStream
import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "nl.designlama.pakkiepakkie"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        applicationId = "nl.designlama.pakkiepakkie"
        vectorDrawables.useSupportLibrary = true
        addManifestPlaceholders(
            mapOf(
                "oidcRedirectScheme" to "pakkiepakkieapp://",
                "appLabel" to "PakkiePakkie"
            )
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    val developSigning = resolveSigning("develop")
    signingConfigs {
        create("develop") {
            if (developSigning != null) {
                storeFile = developSigning.keystore
                storePassword = developSigning.storePassword
                keyAlias = developSigning.keyAlias
                keyPassword = developSigning.keyPassword
            } else {
                println("Variant develop (${project.name}) has NOT signing configured!")
            }
        }
    }

    flavorDimensions += listOf("environment")
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            signingConfig = signingConfigs.getByName("develop")
            buildConfigField(
                type = "nl.designlama.pakkiepakkie.utils.AppConfig.Environment",
                name = "ENVIRONMENT",
                value = "nl.designlama.pakkiepakkie.utils.AppConfig.Environment.DEVELOP"
            )
        }
        create("stag") {
            dimension = "environment"
            applicationIdSuffix = ".stag"
            signingConfig = signingConfigs.getByName("develop")
            buildConfigField(
                type = "nl.designlama.pakkiepakkie.utils.AppConfig.Environment",
                name = "ENVIRONMENT",
                value = "nl.designlama.pakkiepakkie.utils.AppConfig.Environment.STAGING"
            )
        }
        create("prod") {
            dimension = "environment"
            signingConfig = signingConfigs.getByName("develop")
            buildConfigField(
                type = "nl.designlama.pakkiepakkie.utils.AppConfig.Environment",
                name = "ENVIRONMENT",
                value = "nl.designlama.pakkiepakkie.utils.AppConfig.Environment.PRODUCTION"
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            signingConfig = signingConfigs.getByName("develop")
            isDebuggable = true
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activityCompose)
    implementation(libs.material)
    implementation(libs.oidc.ktor)
    implementation(libs.oidc.appsupport)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

private fun Project.resolveSigning(name: String): Signing? {
    var signing = Signing.fromEnv(
        "SIGN_KEYSTORE_BASE64",
        "SIGN_KEYSTORE_PASSWORD",
        "SIGN_KEY_ALIAS",
        "SIGN_KEY_PASSWORD"
    )
    if (signing == null) {
        val filePath = "/signing/keystore.${name}.properties"
        signing = Signing.fromProperties(this, filePath)
    }
    return signing
}

class Signing(
    val keystore: File,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
) {
    companion object {
        fun fromEnv(
            envNameStoreBase64: String,
            envNameStorePassword: String,
            envNameKeyAlias: String,
            envNameKeyPassword: String,
        ): Signing? {
            val keystoreBase64 = System.getenv(envNameStoreBase64) ?: return null
            val keystoreBytes = Base64.getDecoder().decode(keystoreBase64)
            val tempFile = File.createTempFile("keystore", ".keystore")
            tempFile.outputStream().use { it.write(keystoreBytes) }

            val storePassword = System.getenv(envNameStorePassword) ?: return null
            val alias = System.getenv(envNameKeyAlias) ?: return null
            val keyPassword = System.getenv(envNameKeyPassword) ?: return null

            return Signing(tempFile, storePassword, alias, keyPassword)
        }

        fun fromProperties(project: Project, fileName: String): Signing? {
            val propsFile = File(project.rootDir, fileName)
            if (!propsFile.exists()) {
                return null
            }

            val props = Properties().apply { load(FileInputStream(propsFile)) }
            val keystoreFile = File(project.rootDir, props.getProperty("keystore"))
            return Signing(
                keystoreFile,
                props.getProperty("keystore.storePassword"),
                props.getProperty("keystore.alias"),
                props.getProperty("keystore.keyPassword"),
            )
        }
    }
}
