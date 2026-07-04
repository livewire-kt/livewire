plugins {
  id("livewire.kmp.library")
  id("livewire.publish")
  id("livewire.compose")
  alias(libs.plugins.kotlinSerialization)
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
    withHostTest {
      isReturnDefaultValues = true
    }
    withDeviceTest {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  }

  applyDefaultHierarchyTemplate()

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

tasks.matching { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }.configureEach {
  enabled = false
}
