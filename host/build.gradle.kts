import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  id("livewire.compose")
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll(
      "-Xexplicit-backing-fields",
      "-Xcontext-sensitive-resolution",
    )
  }

  jvm()

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      implementation(projects.runtime)
      implementation(projects.ui)

      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.uiToolingPreview)
      implementation(compose.desktop.currentOs)

      implementation(libs.kotlin.reflect)
      implementation(libs.kotlinx.coroutinesSwing)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ktor.serverCore)
      implementation(libs.ktor.serverCio)
      implementation(libs.ktor.serverWebsockets)
      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientCio)
      implementation(libs.ktor.clientWebsockets)
      implementation(libs.ktor.network)
      implementation(libs.dadb)
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor3)
      implementation(libs.coil.svg)
      implementation(libs.dd.plist)
      implementation(libs.multiplatformSettings)
      implementation(libs.multiplatformSettings.noArg)
      implementation(libs.multiplatformSettings.coroutines)
    }
  }
}

compose.desktop {
  application {
    mainClass = "com.r0adkll.livewire.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.r0adkll.livewire.host"
      packageVersion = "1.0.0"
    }
  }
}
