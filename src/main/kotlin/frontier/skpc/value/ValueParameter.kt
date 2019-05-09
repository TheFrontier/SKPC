package frontier.skpc.value

import org.spongepowered.api.text.Text

data class ValueParameter<in P, out T>(val parser: ValueParser<P, T>,
                                       val completer: ValueCompleter<P>,
                                       val usage: ValueUsage) {

    operator fun invoke(key: Text): Parameter<P, T> =
        Parameter(key, parser, completer, usage)

    operator fun invoke(key: String): Parameter<P, T> =
        Parameter(Text.of(key), parser, completer, usage)
}