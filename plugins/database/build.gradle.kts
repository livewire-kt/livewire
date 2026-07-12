import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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

  targets.withType<KotlinNativeTarget>().configureEach {
    compilations["main"].cinterops {
      create("sqlite3") {
        defFile(project.file("src/nativeInterop/cinterop/sqlite3.def"))
      }
    }
  }

  sourceSets {
    androidMain.dependencies {
      api(libs.kotlinx.coroutines.android)
      implementation(libs.androidx.sqlite.framework)
    }
    commonMain.dependencies {
      api(projects.ui)
      api(libs.compose.runtime)
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    iosMain.dependencies {
      implementation(libs.compose.runtime)
    }
    jvmMain.dependencies {
      api(libs.kotlinx.coroutinesSwing)
      implementation(libs.sqlite.jdbc)
    }
  }
}
