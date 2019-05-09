package frontier.skpc.value

import org.spongepowered.api.text.Text

data class Parameter<in P, out T>(val key: Text,
                                  val parser: ValueParser<P, T>,
                                  val completer: ValueCompleter<P>,
                                  val usage: ValueUsage)