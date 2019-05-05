package frontier.skpc.value

import frontier.skpc.util.RTuple
import frontier.skpc.util.component1
import frontier.skpc.util.component2
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.Text

typealias ValueUsage<P> = (src: CommandSource, key: Text, previous: P) -> Text

inline fun usage(crossinline block: (src: CommandSource, key: Text) -> Text): ValueUsage<Any?> =
    { src, key, _ ->
        block(src, key)
    }

inline fun <reified A> usage(
    crossinline block: (src: CommandSource, key: Text, a: A) -> Text): ValueUsage<RTuple<A, *>> =
    { src, args, (a) ->
        block(src, args, a)
    }

inline fun <reified A, reified B> usage(
    crossinline block: (src: CommandSource, key: Text, a: A, b: B) -> Text): ValueUsage<RTuple<A, RTuple<B, *>>> =
    { src, args, (a, b) ->
        block(src, args, a, b)
    }