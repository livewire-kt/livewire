package com.livewire.buildlogic

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val LIVEWIRE_GROUP = "com.livewire-kt"
private const val VERSION_PROPERTY = "livewire.version"

/**
 * Applies the vanniktech maven-publish plugin and configures publishing of a library
 * module to the `com.livewire-kt` group on Maven Central (Central Portal).
 *
 * Each module defines its own coordinates in its `gradle.properties` via the standard
 * vanniktech keys: `POM_ARTIFACT_ID`, `POM_NAME`, and optionally `POM_PACKAGING`. When a
 * module omits `POM_ARTIFACT_ID`/`POM_NAME`, they fall back to a value derived from the
 * Gradle project path, e.g. `:plugins:network:core` -> `livewire-plugins-network-core`.
 *
 * The version is read from the `livewire.version` Gradle property (root gradle.properties).
 *
 * SNAPSHOT versions are routed to the Central Portal snapshots repository and are
 * published unsigned (vanniktech skips signing for `-SNAPSHOT` versions), so no GPG
 * keys are required for snapshot / `publishToMavenLocal` builds.
 */
class LivewirePublishConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("com.vanniktech.maven.publish.base")

    // Per-module publishing metadata comes from the module's own gradle.properties
    // (POM_ARTIFACT_ID / POM_NAME / POM_PACKAGING), matching the vanniktech convention.
    // When a module doesn't define them, fall back to values derived from the Gradle path,
    // e.g. ":plugins:network:core" -> "livewire-plugins-network-core".
    // NOTE: use findProperty (not providers.gradleProperty) for the POM_* keys — the latter
    // only reads root / GRADLE_USER_HOME properties, whereas these live in each module's own
    // gradle.properties. The version lives in the root gradle.properties, so providers is fine.
    val defaultArtifactId = "livewire-" + path.removePrefix(":").replace(':', '-')
    val artifactId = (findProperty("POM_ARTIFACT_ID") as String?) ?: defaultArtifactId
    val artifactName = (findProperty("POM_NAME") as String?) ?: artifactId
    val packaging = findProperty("POM_PACKAGING") as String?
    val version = providers.gradleProperty(VERSION_PROPERTY).get()

    extensions.configure<MavenPublishBaseExtension> {
      publishToMavenCentral()
      signAllPublications()
      coordinates(LIVEWIRE_GROUP, artifactId, version)
      pom {
        name.set(artifactName)
        packaging?.let { this.packaging = it }
        description.set(
          "Livewire — a live development bridge between desktop and mobile Compose apps.",
        )
        inceptionYear.set("2026")
        url.set("https://livewire-kt.com")
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("livewire-kt")
            name.set("Livewire")
            url.set("https://github.com/livewire-kt")
          }
        }
        scm {
          url.set("https://github.com/livewire-kt/livewire")
          connection.set("scm:git:git://github.com/livewire-kt/livewire.git")
          developerConnection.set("scm:git:ssh://git@github.com/livewire-kt/livewire.git")
        }
      }
    }
  }
}
