import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.ksp)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  jvm()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    androidMain.dependencies {
      api(libs.kotlinx.coroutines.android)
    }
    commonMain.dependencies {
      api(projects.runtime)
      api(libs.compose.runtime)
      api(libs.compose.foundation)
      api(libs.compose.ui)
      api(libs.compose.material3)
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.serialization.protobuf)
      api(libs.kotlinx.coroutines.core)
      api(libs.molecule.runtime)
      api(libs.okio)
      api(libs.coil.compose)
      api(libs.coil.network.ktor3)
      api(libs.coil.svg)
      api(libs.ktor.clientWebsockets)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
    jvmMain.dependencies {
      api(libs.kotlinx.coroutinesSwing)
    }
  }
}

dependencies {
  add("kspJvm", projects.compiler)
  add("kspAndroid", projects.compiler)
  add("kspIosArm64", projects.compiler)
  add("kspIosSimulatorArm64", projects.compiler)
}

android {
  namespace = "com.r0adkll.livewire.ui"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
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

composeCompiler {
  stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}
