import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.sqldelight)
}

sqldelight {
  databases {
    create("LivewireDatabase") {
      packageName.set("com.r0adkll.livewire.app")
      srcDirs.setFrom("src/androidMain/sqldelight")
      generateAsync.set(true)
    }
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
    freeCompilerArgs.add("-Xexplicit-backing-fields")
  }

  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  jvm()
  iosArm64()
  iosSimulatorArm64()

  targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
    binaries.framework {
      baseName = "ComposeApp"
      export(projects.runtime)
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.activity.compose)

      implementation(projects.client)
      implementation(projects.plugins.database)

      implementation(libs.sqldelight.android.driver)
      implementation(libs.ktor.clientAndroid)
    }
    commonMain.dependencies {
      implementation(projects.client)
      implementation(projects.plugins.playground)
      implementation(projects.plugins.network.core)
      implementation(projects.plugins.network.ktor)
      api(projects.runtime)

      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.sqldelight.coroutines.extensions)

      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.serializationKotlinxJson)
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor3)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.clientCio)
    }
    iosMain.dependencies {
      implementation(libs.compose.ui)
      implementation(projects.plugins.database)
      implementation(libs.ktor.clientDarwin)
    }
  }
}

android {
  namespace = "com.r0adkll.livewire"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.r0adkll.livewire"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  debugImplementation(libs.compose.uiTooling)
}
