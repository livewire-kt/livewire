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
    freeCompilerArgs.addAll(
      "-Xcontext-parameters",
      "-Xexplicit-backing-fields",
      "-Xcontext-sensitive-resolution",
    )
  }

  android {
    namespace = "com.r0adkll.livewire.client"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  jvm()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      api(projects.runtime)
      api(projects.ui)

      implementation(libs.compose.runtime)
      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientWebsockets)
      implementation(libs.ktor.network)
      implementation(libs.molecule.runtime)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    androidMain.dependencies {
      implementation(libs.kotlin.reflect)
      implementation(libs.ktor.clientOkhttp)
      implementation(libs.androidx.startup)
    }
    iosMain.dependencies {
      implementation(libs.ktor.clientDarwin)
    }
    jvmMain.dependencies {
      implementation(libs.kotlin.reflect)
      implementation(libs.ktor.clientCio)
      implementation(libs.kotlinx.coroutinesSwing)
    }
  }
}


composeCompiler {
  stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}
