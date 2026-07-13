plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.livewire"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1.1")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.livewire.theme"
        name = "Livewire Theme"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }
    instrumentCode = false
    buildSearchableOptions = false
}
