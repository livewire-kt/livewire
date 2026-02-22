import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
  }

  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  jvm()

  sourceSets {
    androidMain.dependencies {
      implementation(libs.ktor.serverCio)
      implementation(libs.ktor.serverWebsockets)
      implementation(libs.compose.runtime)
      implementation(libs.kotlin.reflect)
      implementation(libs.molecule.runtime)
    }
    commonMain.dependencies {
      api(projects.runtime)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      implementation(libs.kotlinx.coroutinesSwing)
    }
  }
}


android {
  namespace = "com.r0adkll.livewire"
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
