import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  id("livewire.kmp.library")
  id("livewire.compose")
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.sqldelight)
}

sqldelight {
  databases {
    create("LivewireDatabase") {
      packageName.set("com.livewire.app")
      srcDirs.setFrom("src/commonMain/sqldelight")
      generateAsync.set(true)
    }
  }
}

kotlin {
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }

  compilerOptions {
    freeCompilerArgs.addAll(
      "-Xcontext-parameters",
      "-Xexplicit-backing-fields",
      "-Xcontext-sensitive-resolution",
    )
  }

  targets.withType<KotlinNativeTarget>().configureEach {
    binaries.framework {
      baseName = "ComposeApp"
      export(projects.runtime)
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.client)
      implementation(projects.plugins.database)
      implementation(projects.plugins.playground)
      implementation(projects.plugins.network.core)
      implementation(projects.plugins.network.ktor)
      api(projects.runtime)

      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.sqldelight.coroutines.extensions)

      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.serializationKotlinxJson)
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor3)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    androidMain.dependencies {
      implementation(projects.plugins.recomposition)

      implementation(libs.ktor.clientCio)
      implementation(libs.sqldelight.android.driver)
    }
    jvmMain.dependencies {
      implementation(projects.plugins.recomposition)

      implementation(libs.ktor.clientCio)
      implementation(libs.kotlinx.coroutinesSwing)
      implementation(libs.sqldelight.sqlite.driver)
    }
    iosMain.dependencies {
      implementation(projects.plugins.recomposition)

      implementation(libs.compose.ui)
      implementation(libs.ktor.clientDarwin)
      implementation(libs.sqldelight.native.driver)
    }
    // No sqldelight driver on web: the demo database exists for the DatabasePlugin to
    // inspect, which isn't available in browsers (no filesystem to scan).
    wasmJsMain.dependencies {
      implementation(libs.ktor.clientJs)
    }
  }
}
