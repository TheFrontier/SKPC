package frontier.skpc.value

import frontier.skpc.util.RTuple
import frontier.skpc.util.component1
import frontier.skpc.util.component2
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandArgs

typealias ValueCompleter<P> = (src: CommandSource, args: CommandArgs, previous: P) -> List<String>

interface IValueCompleter<P> : ValueCompleter<P> {
    override fun invoke(src: CommandSource, args: CommandArgs, previous: P): List<String>

    companion object {
        inline operator fun <P> invoke(crossinline block: ValueCompleter<P>): IValueCompleter<P> =
            object : IValueCompleter<P> {
                override fun invoke(src: CommandSource, args: CommandArgs, previous: P): List<String> =
                    block(src, args, previous)
            }
    }
}

inline fun completer(crossinline block: (src: CommandSource, args: CommandArgs) -> List<String>): ValueCompleter<Any?> =
    { src, args, _ ->
        block(src, args)
    }

inline fun <reified A> completer(
    crossinline block: (src: CommandSource, args: CommandArgs, a: A) -> List<String>): ValueCompleter<RTuple<A, *>> =
    { src, args, (a) ->
        block(src, args, a)
    }

inline fun <reified A, reified B> completer(
    crossinline block: (src: CommandSource, args: CommandArgs, a: A, b: B) -> List<String>): ValueCompleter<RTuple<A, RTuple<B, *>>> =
    { src, args, (a, b) ->
        block(src, args, a, b)
    }