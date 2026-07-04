plugins {
  id("livewire.kmp.library")
  id("livewire.publish")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.plugins.network.core)
      api(libs.ktor.clientCore)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
