plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  jvm()

  sourceSets {
    commonMain.dependencies {
      api(projects.runtime)
      api(libs.kotlinx.serialization.json)
      implementation(libs.compose.runtime)
      implementation(libs.compose.ui)
      implementation(libs.compose.material3)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }

    jvmMain.dependencies {
    }
  }
}
