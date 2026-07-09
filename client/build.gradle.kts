plugins {
  id("livewire.kmp.library")
  id("livewire.publish")
  id("livewire.compose")
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll(
      "-Xcontext-sensitive-resolution",
    )
  }

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
      implementation(libs.ktor.clientCio)
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
