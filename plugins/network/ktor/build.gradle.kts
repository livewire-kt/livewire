import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  id("livewire.kmp.library")
  id("livewire.publish")
}

kotlin {
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }

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
