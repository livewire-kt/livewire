plugins {
  id("livewire.kmp.library")
  id("livewire.publish")
  id("livewire.compose")
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.ksp)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  sourceSets {
    androidMain.dependencies {
      api(libs.kotlinx.coroutines.android)
    }
    commonMain.dependencies {
      api(projects.runtime)
      api(libs.compose.runtime)
      api(libs.compose.foundation)
      api(libs.compose.ui)
      api(libs.compose.material3)
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.serialization.protobuf)
      api(libs.kotlinx.coroutines.core)
      api(libs.molecule.runtime)
      api(libs.okio)
      api(libs.coil.compose)
      api(libs.coil.network.ktor3)
      implementation(libs.jsontree)
      api(libs.ktor.clientWebsockets)
      implementation(libs.stately.concurrent.collections)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
    jvmMain.dependencies {
      api(libs.kotlinx.coroutinesSwing)
    }
  }
}

dependencies {
  add("kspJvm", projects.compiler)
  add("kspAndroid", projects.compiler)
  add("kspIosArm64", projects.compiler)
  add("kspIosSimulatorArm64", projects.compiler)
}
