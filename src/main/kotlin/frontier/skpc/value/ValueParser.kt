package frontier.skpc.value

import frontier.skpc.util.RTuple
import frontier.skpc.util.component1
import frontier.skpc.util.component2
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandArgs

typealias ValueParser<P, T> = (src: CommandSource, args: CommandArgs, previous: P) -> T

interface IValueParser<P, T> : ValueParser<P, T> {
    override fun invoke(src: CommandSource, args: CommandArgs, previous: P): T

    companion object {
        inline operator fun <P, T> invoke(crossinline block: ValueParser<P, T>): IValueParser<P, T> =
            object : IValueParser<P, T> {
                override fun invoke(src: CommandSource, args: CommandArgs, previous: P): T =
                    block(src, args, previous)
            }
    }
}

inline fun <reified T> parser(crossinline block: (src: CommandSource, args: CommandArgs) -> T): ValueParser<Any?, T> =
    { src, args, _ ->
        block(src, args)
    }

inline fun <reified A, T> parser(
    crossinline block: (src: CommandSource, args: CommandArgs, a: A) -> T): ValueParser<RTuple<A, *>, T> =
    { src, args, (a) ->
        block(src, args, a)
    }

inline fun <reified A, reified B, T> parser(
    crossinline block: (src: CommandSource, args: CommandArgs, a: A, b: B) -> T): ValueParser<RTuple<A, RTuple<B, *>>, T> =
    { src, args, (a, b) ->
        block(src, args, a, b)
    }