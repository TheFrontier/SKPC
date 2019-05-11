import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    maven
    java
    kotlin("jvm") version "1.3.21"
}

group = "io.github.thefrontier"
version = "0.3.0"

repositories {
    mavenCentral()
    jcenter()
    maven {
        setUrl("https://jitpack.io")
    }
    maven {
        setUrl("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("reflect"))

    compileOnly("org.spongepowered:spongeapi:7.1.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}