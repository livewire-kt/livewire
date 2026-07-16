package com.livewire.buildlogic

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val LIVEWIRE_GROUP = "com.livewire-kt.livewire"
private const val VERSION_PROPERTY = "livewire.version"

class LivewirePublishConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("com.vanniktech.maven.publish.base")

    val defaultArtifactId = "livewire-" + path.removePrefix(":").replace(':', '-')
    val artifactId = (findProperty("POM_ARTIFACT_ID") as String?) ?: defaultArtifactId
    val artifactName = (findProperty("POM_NAME") as String?) ?: artifactId
    val packaging = findProperty("POM_PACKAGING") as String?
    val version = providers.gradleProperty(VERSION_PROPERTY).get()

    extensions.configure<MavenPublishBaseExtension> {
      publishToMavenCentral()
      signAllPublications()
      configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
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
