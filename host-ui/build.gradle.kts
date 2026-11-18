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
      val jsonPath = libs.jsonpath4k.get()
      implementation("${jsonPath.group}:${jsonPath.name}:${jsonPath.version}") {
        exclude(group = "io.github.aakira", module = "napier")
      }
    }
    iosMain.dependencies {
      // The published JSONPath4K iOS KLIB declares Napier as a linker dependency.
      implementation(libs.napier)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
    jvmTest.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutinesSwing)
    }
  }
}
