import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    maven
    java
    kotlin("jvm") version "1.3.21"
}

group = "io.github.thefrontier"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        setUrl("https://jitpack.io")
    }
    maven {
        name = "sponge"
        setUrl("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("reflect"))

    compileOnly("org.spongepowered:spongeapi:7.1.0")

    compileOnly("com.github.TheFrontier:SKE:8618c9d738")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

//task("codegen") {
//    val tuples = generateTuples(10)
//    val ands = generateAnds(10)
//
//    val tuplesFile = kotlin.sourceSets["main"].kotlin.srcDirs.first().absoluteFile.resolve("generated/tuples.kt")
//    val andsFile = kotlin.sourceSets["main"].kotlin.srcDirs.first().absoluteFile.resolve("generated/andCombinators.kt")
//
//    tuplesFile.parentFile.mkdirs()
//    andsFile.parentFile.mkdirs()
//
//    if (tuplesFile.exists()) tuplesFile.delete()
//    if (andsFile.exists()) andsFile.delete()
//
//    tuplesFile.writeText(tuples)
//    andsFile.writeText(ands)
//}
//
//fun generateTuples(max: Int): String {
//    return buildString {
//        appendln("package frontier.skpc.util")
//        appendln()
//
//        for (i in 1..max) {
//            val typeParams = (1..i).joinToString(prefix = "<", postfix = ">") { "T$it" }
//            val valueParams = (1..i).joinToString { "val t$it: T$it" }
//            val components = (1..i).joinToString { "t$it" }
//            val bounds = (1..i).joinToString { "T$it : T" }
//
//            appendln(
//                """
//                data class Tuple$i$typeParams($valueParams) : Tuple
//
//                val <T, $bounds> Tuple$i$typeParams.components
//                    get() = listOf($components)
//                """.trimIndent()
//            )
//
//            appendln()
//        }
//    }
//}
//
//fun generateAnds(max: Int): String {
//    return buildString {
//        appendln("package frontier.skpc.combinator")
//        appendln()
//        appendln("import frontier.skpc.util.*")
//        appendln("import frontier.skpc.element.*")
//        appendln()
//
//        for (i in 2 until max) {
//            val typeParams = (1..i).joinToString(prefix = "<", postfix = ">") { "T$it" }
//            val reifieds = (1..i + 1).joinToString { "reified T$it" }
//            val casts = (1..i + 1).joinToString { "it[${it - 1}] as T$it" }
//
//            appendln(
//                """
//                @JvmName("and$i")
//                inline infix fun <$reifieds> AndCombinator<Tuple$i$typeParams>.and(p${i + 1}: Element<T${i + 1}>) =
//                    AndCombinator(consumers + p${i + 1}) {
//                        Tuple${i + 1}($casts)
//                    }
//                """.trimIndent()
//            )
//            appendln()
//
//            appendln(
//                """
//                @JvmName("andOperator$i")
//                inline operator fun <$reifieds> AndCombinator<Tuple$i$typeParams>.times(p${i + 1}: Element<T${i + 1}>) =
//                    this and p${i + 1}
//                """.trimIndent()
//            )
//            appendln()
//        }
//    }
//}