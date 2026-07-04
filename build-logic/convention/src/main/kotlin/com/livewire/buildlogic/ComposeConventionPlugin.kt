package com.livewire.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class ComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("org.jetbrains.compose")
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    extensions.configure<ComposeCompilerGradlePluginExtension> {
      includeSourceInformation.set(true)
      stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("stability_config.conf"),
      )
    }
  }
}
