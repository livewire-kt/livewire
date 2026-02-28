import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  jvm()

  sourceSets {
    commonMain.dependencies {
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
      implementation(libs.compose.components.resources)
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
      implementation(libs.dadb)
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor3)
      implementation(libs.coil.svg)
    }
  }
}

val buildIosBridge = tasks.register<Exec>("buildIosBridge") {
  val bridgeDir = rootProject.layout.projectDirectory.dir("tools/ios-device-bridge")
  workingDir = bridgeDir.asFile
  commandLine("cargo", "build", "--release")

  inputs.file(bridgeDir.file("Cargo.toml"))
  inputs.dir(bridgeDir.dir("src"))
  outputs.file(bridgeDir.file("target/release/livewire-ios-bridge"))
}

tasks.matching { it.name == "compileKotlinJvm" }.configureEach {
  dependsOn(buildIosBridge)
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
