import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinJvm)
  id("livewire.compose")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll(
      "-Xskip-prerelease-check",
    )
  }
}

dependencies {
  implementation(projects.demo.common)
  implementation(projects.client)
  implementation(projects.plugins.database)
  implementation(projects.plugins.network.core)
  implementation(projects.plugins.playground)
  implementation(projects.plugins.recomposition)

  implementation(compose.desktop.currentOs)
  implementation(libs.kotlinx.coroutinesSwing)
}

compose.desktop {
  application {
    mainClass = "com.livewire.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.livewire.client"
      packageVersion = "1.0.0"
    }
  }
}
