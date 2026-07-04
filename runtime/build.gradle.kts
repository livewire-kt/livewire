plugins {
  id("livewire.kmp.library")
  id("livewire.compose")
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  sourceSets {
    androidMain.dependencies {
      api(libs.kotlinx.coroutines.android)
      implementation(libs.androidx.startup)
    }
    commonMain.dependencies {
      api(libs.compose.runtime)
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.coroutines.core)
      implementation(libs.cryptography.core)
      implementation(libs.cryptography.provider.optimal)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      api(libs.kotlinx.coroutinesSwing)
    }
  }
}
