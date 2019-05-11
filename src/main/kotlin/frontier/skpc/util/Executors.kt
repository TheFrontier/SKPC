package frontier.skpc.util

import frontier.skpc.CommandValueExecutor
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.text.Text

inline fun <reified Source, reified NewSource : Source, Value> mustBeSubtype(
    crossinline block: CommandValueExecutor<NewSource, Value>
): CommandValueExecutor<Source, Value> {
    val errorMessage = Text.of("You must be a ${Source::class.simpleName} to use that command!")

    return { src, value ->
        if (src !is NewSource) {
            throw CommandException(errorMessage)
        }

        block(src, value)
    }
}

inline fun <Source, NewSource, Value> transformSource(
    crossinline transform: (Source) -> NewSource,
    crossinline block: CommandValueExecutor<NewSource, Value>
): CommandValueExecutor<Source, Value> = { source, value ->
    val newSource = transform(source)
    block(newSource, value)
}

inline fun <Source, Value, Computed> compute(
    crossinline compute: (Source) -> Computed,
    crossinline block: CommandValueExecutor<Source, RTuple<Computed, Value>>
): CommandValueExecutor<Source, Value> = { source, value ->
    val computed = compute(source)
    block(source, RTuple(computed, value))
}