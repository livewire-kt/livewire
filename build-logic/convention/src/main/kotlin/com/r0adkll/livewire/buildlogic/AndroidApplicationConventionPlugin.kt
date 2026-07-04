package com.r0adkll.livewire.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("com.android.application")

    val compileSdkVersion = catalogVersionInt("android-compileSdk")
    val minSdkVersion = catalogVersionInt("android-minSdk")
    val targetSdkVersion = catalogVersionInt("android-targetSdk")

    extensions.configure<ApplicationExtension> {
      namespace = "com.r0adkll.livewire"
      compileSdk = compileSdkVersion

      defaultConfig {
        applicationId = "com.r0adkll.livewire"
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion
        versionCode = 1
        versionName = "1.0"
      }

      packaging {
        resources {
          excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
      }

      buildTypes {
        getByName("release") {
          isMinifyEnabled = false
        }
      }

      compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
      }
    }
  }
}
