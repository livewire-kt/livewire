import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  id("livewire.kmp.library")
  id("livewire.publish")
  id("livewire.compose")
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }

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
      implementation(libs.molecule.runtime)
    }

    // Raw-socket discovery transports (UDP broadcast / TCP server) shared by every target
    // except web — browsers have no socket APIs and ktor-network's wasm artifact only runs
    // under Node, so it must stay off the wasmJs compilation path entirely. Shared via srcDir
    // instead of an intermediate source set to keep the default hierarchy template intact.
    val socketTransports = "src/commonSocket/kotlin"
    androidMain {
      kotlin.srcDir(socketTransports)
      dependencies {
        implementation(libs.ktor.network)
      }
    }
    jvmMain {
      kotlin.srcDir(socketTransports)
      dependencies {
        implementation(libs.ktor.network)
      }
    }
    iosMain {
      kotlin.srcDir(socketTransports)
      dependencies {
        implementation(libs.ktor.network)
      }
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
    wasmJsMain.dependencies {
      implementation(libs.ktor.clientJs)
    }
  }
}
