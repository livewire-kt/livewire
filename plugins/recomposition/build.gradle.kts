import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll(
      "-Xcontext-parameters",
      "-Xexplicit-backing-fields",
      "-Xcontext-sensitive-resolution",
    )
  }

  android {
    namespace = "com.r0adkll.livewire.plugin.recomposition"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
    withHostTest {
      isReturnDefaultValues = true
    }
    withDeviceTest {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  }

  jvm()

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    val jvmSharedMain by creating {
      dependsOn(commonMain.get())
    }

    commonMain {
      dependencies {
        api(projects.ui)
        api(libs.compose.runtime)
        api(libs.kotlinx.serialization.json)
        api(libs.kotlinx.coroutines.core)
        implementation(libs.stately.concurrent.collections)
      }
    }

    commonTest {
      dependencies {
        implementation(libs.kotlin.test)
      }
    }

    getByName("androidDeviceTest") {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.test.ext.junit)
      }
    }

    androidMain {
      dependsOn(jvmSharedMain)
      dependencies {
        api(libs.kotlinx.coroutines.android)
        implementation(libs.androidx.startup)
      }
    }

    jvmMain {
      dependsOn(jvmSharedMain)
      dependencies {
        api(libs.kotlinx.coroutinesSwing)
      }
    }
  }
}

composeCompiler {
  includeSourceInformation = true
  stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}

tasks.matching { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }.configureEach {
  enabled = false
}
