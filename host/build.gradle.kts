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
      implementation(projects.hostUi)

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
      implementation(libs.dd.plist)
      implementation(libs.multiplatformSettings)
      implementation(libs.multiplatformSettings.noArg)
      implementation(libs.multiplatformSettings.coroutines)
    }
  }
}

compose.desktop {
  application {
    mainClass = "com.livewire.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.livewire.host"
      packageVersion = "1.0.0"

      macOS {
        iconFile.set(project.file("icons/macos/icon.icns"))
      }
      windows {
        iconFile.set(project.file("icons/windows/icon.ico"))
      }
      linux {
        iconFile.set(project.file("icons/linux/icon.png"))
      }
    }
  }
}
