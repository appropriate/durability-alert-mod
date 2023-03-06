plugins {
  `maven-publish`
  alias(libs.plugins.quilt.loom)
  jacoco
  checkstyle
  alias(libs.plugins.spotbugs)
}

// To change the versions see the gradle.properties file
val archives_base_name: String by project
val maven_group: String by project

version = "${project.version}+${libs.versions.minecraft.get()}"
group = maven_group

base {
  archivesName.set(archives_base_name)
}

repositories {
  maven { url = uri("https://maven.shedaniel.me/") }
  maven { url = uri("https://maven.terraformersmc.com/releases/") }
}

dependencies {
  minecraft(libs.minecraft)
  mappings(variantOf(libs.quilt.mappings) { classifier("intermediary-v2") })
	modImplementation(libs.quilt.loader)

	// QSL is not a complete API; You will need Quilted Fabric API to fill in the gaps.
	// Quilted Fabric API will automatically pull in the correct QSL version.
	modImplementation(libs.quilted.fabric.api)

  // Cloth Config
  modApi(libs.cloth.config.fabric) {
    exclude("net.fabricmc", "fabric-loader")
    exclude("net.fabricmc.fabric-api")
  }

  // Modmenu
  modImplementation(libs.modmenu) {
    exclude("net.fabricmc", "fabric-loader")
  }

  // PSA: Some older mods, compiled on Loom 0.2.1, might have outdated Maven POMs.
  // You may need to force-disable transitiveness on them.

  testImplementation(libs.quilt.loader.junit)
  testImplementation(platform("org.junit:junit-bom:5.9.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")

  // Google Truth
  testImplementation(libs.bundles.truth)

  // Work around bad interaction between MC's use of Guava and Truth's
  testRuntimeOnly("com.google.guava:guava:30.1-jre")

  // Include SpotBugs annotations for compilation of main and test
  compileOnly(libs.spotbugs.annotations)
  testCompileOnly(libs.spotbugs.annotations)
}

tasks.processResources {
  val properties = mapOf(
    "version" to project.version,
    "java_version" to java.targetCompatibility.majorVersion,
    "loader_version" to libs.versions.quilt.loader.get(),
    "api_version" to libs.versions.quilted.fabric.api.get(),
    "minecraft_version" to libs.versions.minecraft.get(),
    "cloth_config_version" to libs.versions.cloth.config.get(),
    "modmenu_version" to libs.versions.modmenu.get())

  inputs.properties(properties)

  filesMatching("quilt.mod.json") {
    expand(properties)
  }
}

tasks.withType<JavaCompile> {
  // Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
  options.release.set(17)

  options.compilerArgs.add("-Xlint:deprecation")
  options.compilerArgs.add("-Xlint:unchecked")
}

checkstyle {
  config = resources.text.fromArchiveEntry(
    configurations.checkstyle.get().find { it.name.contains("checkstyle") }!!,
    "google_checks.xml"
  )
  maxErrors = 0
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17

  // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
  // if it is present.
  // If you remove this line, sources will not be generated.
  withSourcesJar()
}

tasks.jar {
  from("LICENSE") {
    rename { "${it}_${base.archivesName}"}
  }
}

tasks.test {
  useJUnitPlatform()
  workingDir(project.file("run"))
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
  testLogging {
    events("passed", "skipped", "failed")
  }
}

spotbugs {
  toolVersion.set(libs.versions.spotbugs)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // tests are required to run before generating the report
}

// configure the maven publication
publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }

  // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
  repositories {
    // Add repositories to publish to here.
    // Notice: This block does NOT have the same function as the block in the top level.
    // The repositories here will be used for publishing your artifact, not for
    // retrieving dependencies.
  }
}

tasks.register<Copy>("copyToLocalMinecraft") {
  dependsOn(tasks.build)

  from(tasks.remapJar)

  into("${System.getProperty("user.home")}/Library/Application Support/minecraft/mods")

  eachFile { println("Copying ${name} to ${relativePath.getFile(destinationDir)}") }
}
