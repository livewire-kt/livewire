import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  android {
    namespace = "com.r0adkll.livewire.plugin.database"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  jvm()
  iosArm64()
  iosSimulatorArm64()

  targets.withType<KotlinNativeTarget>().configureEach {
    compilations["main"].cinterops {
      create("sqlite3") {
        defFile(project.file("src/nativeInterop/cinterop/sqlite3.def"))
      }
    }
  }

  sourceSets {
    androidMain.dependencies {
      api(libs.kotlinx.coroutines.android)
      implementation(libs.androidx.sqlite.framework)
    }
    commonMain.dependencies {
      api(projects.ui)
      api(libs.compose.runtime)
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    iosMain.dependencies {
      implementation(libs.compose.runtime)
    }
    jvmMain.dependencies {
      api(libs.kotlinx.coroutinesSwing)
      implementation(libs.sqlite.jdbc)
    }
  }
}


composeCompiler {
  includeSourceInformation = true
  stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}
