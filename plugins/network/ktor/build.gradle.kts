plugins {
  id("livewire.kmp.library")
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
