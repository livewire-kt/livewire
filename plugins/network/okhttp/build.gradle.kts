plugins {
  id("livewire.kmp.library.jvmonly")
  id("livewire.publish")
}

kotlin {
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
