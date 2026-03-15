import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.sqldelight)
}

sqldelight {
  databases {
    create("LivewireDatabase") {
      packageName.set("com.r0adkll.livewire.app")
      srcDirs.setFrom("src/commonMain/sqldelight")
      generateAsync.set(true)
    }
  }
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
    namespace = "com.r0adkll.livewire.demo.common"
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
    binaries.framework {
      baseName = "ComposeApp"
      export(projects.runtime)
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.client)
      implementation(projects.plugins.database)
      implementation(projects.plugins.playground)
      implementation(projects.plugins.network.core)
      implementation(projects.plugins.network.ktor)
      api(projects.runtime)

      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.sqldelight.coroutines.extensions)

      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.serializationKotlinxJson)
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor3)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    androidMain.dependencies {
      implementation(libs.ktor.clientOkhttp)
      implementation(libs.sqldelight.android.driver)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.clientCio)
      implementation(libs.kotlinx.coroutinesSwing)
      implementation(libs.sqldelight.sqlite.driver)
    }
    iosMain.dependencies {
      implementation(libs.compose.ui)
      implementation(libs.ktor.clientDarwin)
      implementation(libs.sqldelight.native.driver)
    }
  }
}

composeCompiler {
  stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
}
