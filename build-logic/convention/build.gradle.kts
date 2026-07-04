plugins {
  `kotlin-dsl`
}

group = "com.livewire.buildlogic"

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.composeCompiler.gradlePlugin)
  compileOnly(libs.vanniktech.mavenPublish.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("kmpLibrary") {
      id = "livewire.kmp.library"
      implementationClass = "com.livewire.buildlogic.KmpLibraryConventionPlugin"
    }
    register("kmpLibraryJvmOnly") {
      id = "livewire.kmp.library.jvmonly"
      implementationClass = "com.livewire.buildlogic.KmpLibraryJvmOnlyConventionPlugin"
    }
    register("compose") {
      id = "livewire.compose"
      implementationClass = "com.livewire.buildlogic.ComposeConventionPlugin"
    }
    register("androidApplication") {
      id = "livewire.android.application"
      implementationClass = "com.livewire.buildlogic.AndroidApplicationConventionPlugin"
    }
    register("jvmLibrary") {
      id = "livewire.jvm.library"
      implementationClass = "com.livewire.buildlogic.JvmLibraryConventionPlugin"
    }
    register("publish") {
      id = "livewire.publish"
      implementationClass = "com.livewire.buildlogic.LivewirePublishConventionPlugin"
    }
  }
}
