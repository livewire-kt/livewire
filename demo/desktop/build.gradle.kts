import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
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

  implementation(compose.desktop.currentOs)
  implementation(libs.kotlinx.coroutinesSwing)
}

compose.desktop {
  application {
    mainClass = "com.r0adkll.livewire.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.r0adkll.livewire.client"
      packageVersion = "1.0.0"
    }
  }
}

composeCompiler {
  stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}
