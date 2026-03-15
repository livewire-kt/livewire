import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
}

kotlin {
  android {
    namespace = "com.r0adkll.livewire.plugin.network.okhttp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  jvm()

  sourceSets {
    commonMain.dependencies {
      api(projects.plugins.network.core)
      api(libs.okhttp)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
