import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  android {
    namespace = "com.r0adkll.livewire.plugin.network.core"
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
    androidMain.dependencies {
      api(libs.kotlinx.coroutines.android)
    }
    commonMain.dependencies {
      api(projects.ui)
      api(libs.compose.runtime)
      api(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      api(libs.kotlinx.coroutinesSwing)
    }
  }
}

composeCompiler {
  includeSourceInformation = true
  stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}
