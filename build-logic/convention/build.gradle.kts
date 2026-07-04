plugins {
  `kotlin-dsl`
}

group = "com.r0adkll.livewire.buildlogic"

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.composeCompiler.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("kmpLibrary") {
      id = "livewire.kmp.library"
      implementationClass = "com.r0adkll.livewire.buildlogic.KmpLibraryConventionPlugin"
    }
    register("kmpLibraryJvmOnly") {
      id = "livewire.kmp.library.jvmonly"
      implementationClass = "com.r0adkll.livewire.buildlogic.KmpLibraryJvmOnlyConventionPlugin"
    }
    register("compose") {
      id = "livewire.compose"
      implementationClass = "com.r0adkll.livewire.buildlogic.ComposeConventionPlugin"
    }
    register("androidApplication") {
      id = "livewire.android.application"
      implementationClass = "com.r0adkll.livewire.buildlogic.AndroidApplicationConventionPlugin"
    }
    register("jvmLibrary") {
      id = "livewire.jvm.library"
      implementationClass = "com.r0adkll.livewire.buildlogic.JvmLibraryConventionPlugin"
    }
  }
}
