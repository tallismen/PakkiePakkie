import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildConfig)
}

kotlin {
    androidTarget {
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "nl.designlama.pakkiepakkie.ComposeApp")
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.bundles.koin)
            implementation(libs.ksafe)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(project.dependencies.platform(libs.koin.annotations.bom))
            implementation(libs.coil)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.datetime)
            implementation(libs.room.runtime)
            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.oidc.appsupport)
            implementation(libs.oidc.ktor)
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
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

    }
}

android {
    namespace = "nl.designlama.pakkiepakkie"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    androidTestImplementation(libs.androidx.uitest.junit4)
    debugImplementation(libs.androidx.uitest.testManifest)
}

buildConfig {
    buildConfigField("String", "BASE_URL_API_DEV", "\"https://api-dev.designlama.nl\"")
    buildConfigField("String", "BASE_URL_API_STAG", "\"https://api-staging.designlama.nl\"")
    buildConfigField("String", "BASE_URL_API_PRD", "\"https://api.designlama.nl\"")

    buildConfigField("String", "BASE_URL_AUTH_DEV", "\"https://auth-dev.designlama.nl\"")
    buildConfigField("String", "BASE_URL_AUTH_STAG", "\"https://auth-staging.designlama.nl\"")
    buildConfigField("String", "BASE_URL_AUTH_PRD", "\"https://auth.designlama.nl\"")
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    with(libs.room.compiler) {
        add("kspAndroid", this)
        add("kspIosX64", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
    }

    add("kspCommonMainMetadata", libs.koin.annotations.ksp)
    add("kspAndroid", libs.koin.annotations.ksp)
    add("kspIosX64", libs.koin.annotations.ksp)
    add("kspIosArm64", libs.koin.annotations.ksp)
    add("kspIosSimulatorArm64", libs.koin.annotations.ksp)
}

ksp {
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
}

project.afterEvaluate {
    tasks.named("kspDebugKotlinAndroid") {
        dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
        enabled = false
    }
    tasks.named("kspReleaseKotlinAndroid") {
        dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
        enabled = false
    }
    tasks.named("kspKotlinIosX64") {
        dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
        enabled = false
    }
    tasks.named("kspKotlinIosArm64") {
        dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
        enabled = false
    }
    tasks.named("kspKotlinIosSimulatorArm64") {
        dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
        enabled = false
    }
}

project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
