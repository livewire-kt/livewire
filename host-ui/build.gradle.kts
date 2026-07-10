plugins {
  id("livewire.kmp.library")
  id("livewire.publish")
  id("livewire.compose")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.ui)
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor3)
      implementation(libs.jsontree)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}
