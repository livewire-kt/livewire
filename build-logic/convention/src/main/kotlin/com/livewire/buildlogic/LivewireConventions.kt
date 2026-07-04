package com.livewire.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private const val BASE_PACKAGE = "com.livewire"

internal val Project.libs: VersionCatalog
  get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.catalogVersionInt(alias: String): Int =
  libs.findVersion(alias).get().requiredVersion.toInt()

internal fun Project.deriveNamespace(): String {
  val suffix = path.removePrefix(":").replace(':', '.')
  return if (suffix.isEmpty()) BASE_PACKAGE else "$BASE_PACKAGE.$suffix"
}

internal fun Project.configureKotlinMultiplatformLibrary(withIos: Boolean) {
  val namespace = deriveNamespace()
  val compileSdkVersion = catalogVersionInt("android-compileSdk")
  val minSdkVersion = catalogVersionInt("android-minSdk")

  extensions.configure<KotlinMultiplatformExtension> {
    val android = (this as ExtensionAware).extensions
      .getByName("android") as KotlinMultiplatformAndroidLibraryTarget
    android.namespace = namespace
    android.compileSdk = compileSdkVersion
    android.minSdk = minSdkVersion
    android.compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }

    jvm()

    if (withIos) {
      iosArm64()
      iosSimulatorArm64()
    }
  }
}
