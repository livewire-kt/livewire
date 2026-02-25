plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.autoservice.ir)
}

dependencies {
  implementation(libs.ksp.api)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)
  implementation(projects.runtime)

  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
