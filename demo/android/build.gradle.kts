plugins {
  id("livewire.android.application")
  id("livewire.compose")
}

dependencies {
  implementation(projects.demo.common)
  implementation(projects.client)
  implementation(projects.plugins.database)
  implementation(projects.plugins.network.core)
  implementation(projects.plugins.playground)
  implementation(projects.plugins.recomposition)

  implementation(libs.compose.runtime)
  implementation(libs.androidx.activity.compose)
  implementation(libs.compose.uiToolingPreview)

  debugImplementation(libs.compose.uiTooling)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  compilerOptions {
    freeCompilerArgs.add("-Xskip-prerelease-check")
  }
}
