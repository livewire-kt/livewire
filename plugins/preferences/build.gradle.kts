plugins {
  id("livewire.kmp.library")
  id("livewire.publish")
  id("livewire.compose")
}

kotlin {
  sourceSets {
    androidMain.dependencies {
      api(libs.kotlinx.coroutines.android)
    }
    commonMain.dependencies {
      api(projects.ui)
      api(libs.compose.runtime)
      api(libs.kotlinx.coroutines.core)
      api(libs.androidx.datastore.preferences.core)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    iosMain.dependencies {
      implementation(libs.compose.runtime)
    }
    jvmMain.dependencies {
      api(libs.kotlinx.coroutinesSwing)
    }
  }
}
