plugins {
  id("fabric-loom") version "1.1-SNAPSHOT"
  `maven-publish`
  jacoco
  checkstyle
  id("com.github.spotbugs") version "5.0.13"
}

// To change the versions see the gradle.properties file
val archives_base_name: String by project
val mod_version: String by project
val maven_group: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val truth_version: String by project
val spotbugs_version: String by project

version = mod_version
group = maven_group

base {
  archivesName.set(archives_base_name)
}

repositories {
  maven { url = uri("https://maven.shedaniel.me/") }
  maven { url = uri("https://maven.terraformersmc.com/releases/") }
}

dependencies {
  minecraft("com.mojang:minecraft:$minecraft_version")
  mappings("net.fabricmc:yarn:$yarn_mappings:v2")
  modImplementation("net.fabricmc:fabric-loader:$loader_version")

  // Fabric API. This is technically optional, but you probably want it anyway.
  modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

  // Cloth Config
  modApi("me.shedaniel.cloth:cloth-config-fabric:9.0.94") {
    exclude("net.fabricmc", "fabric-loader")
    exclude("net.fabricmc.fabric-api")
  }

  // Modmenu
  modImplementation("com.terraformersmc:modmenu:5.1.0-beta.4") {
    exclude("net.fabricmc", "fabric-loader")
  }

  // PSA: Some older mods, compiled on Loom 0.2.1, might have outdated Maven POMs.
  // You may need to force-disable transitiveness on them.

  testImplementation("net.fabricmc:fabric-loader-junit:$loader_version")
  testImplementation(platform("org.junit:junit-bom:5.9.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")

  // Google Truth
  testImplementation("com.google.truth:truth:$truth_version")
  testImplementation("com.google.truth.extensions:truth-java8-extension:$truth_version")

  // Work around bad interaction between MC's use of Guava and Truth's
  testRuntimeOnly("com.google.guava:guava:30.1-jre")

  // Include SpotBugs annotations for compilation of main and test
  compileOnly("com.github.spotbugs:spotbugs-annotations:$spotbugs_version")
  testCompileOnly("com.github.spotbugs:spotbugs-annotations:$spotbugs_version")
}

tasks.processResources {
  inputs.property("version", project.version)

  filesMatching("fabric.mod.json") {
    expand(mapOf("version" to project.version))
  }
}

tasks.withType<JavaCompile> {
  // Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
  options.release.set(17)
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
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
  testLogging {
    events("passed", "skipped", "failed")
  }
}

spotbugs {
  toolVersion.set(spotbugs_version)
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
}
