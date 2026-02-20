plugins {
    alias(libs.plugins.kotlinJvm)
}

group = "com.r0adkll.livewire"
version = "unspecified"

dependencies {
    implementation(projects.shared)
    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.clientCio)
    implementation(libs.ktor.clientWebsockets)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}
