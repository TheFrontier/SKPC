# Sponge Kotlin Parser tree Commands

Yet Another Command API (tm)

Features:
- Fully type-safe.
- Easily intermingle subcommands and arguments.
- Write arguments and subcommands that are dependent on previous arguments.
- Compact Kotlin-based DSL makes writing commands simple.

### SKPC in Action

- [arven-perms](https://github.com/Arvenwood/arven-perms/blob/0.1.0/src/main/kotlin/arven/perms/plugin/command/APCommand.kt)


### A Simple Example

```kotlin
import frontier.skpc.CommandTree
import frontier.skpc.util.*
import frontier.skpc.value.standard.ValueParameters.int
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.text.Text

val command = CommandTree.Root("sum").apply {
    int("value1") / int("value2") execute { (value1, value2, src) ->
        src.sendMessage(Text.of("$value1 + $value2 = ${value1 + value2}"))
        CommandResult.success()
    }
}

Sponge.getCommandManager().register(plugin, command.toCallable(), command.aliases)
```

## Get It

### Arven Core (Recommended)

[arven-core](https://github.com/Arvenwood/arven-core) shades SKPC so you don't have to!
Simply put `arven-core` as a dependency in your @Plugin annotation, and have users download it when downloading your plugin.

```kotlin
repositories {
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.Arvenwood:arven-core:<current version>")
}
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.TheFrontier:SKPC:<current version>")
}
```