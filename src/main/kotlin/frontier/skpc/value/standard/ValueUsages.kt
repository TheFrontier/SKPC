package frontier.skpc.value.standard

import frontier.skpc.value.ValueUsage
import org.spongepowered.api.text.Text

object ValueUsages {

    val empty: ValueUsage<Any?> = { _, _, _ ->
        Text.EMPTY
    }

    val identity: ValueUsage<Any?> = { _, key, _ ->
        key
    }

    val single: ValueUsage<Any?> = { _, key, _ ->
        Text.of("<", key, ">")
    }

    val variadic: ValueUsage<Any?> = { _, key, _ ->
        Text.of("<", key, "...>")
    }

    inline fun <P> optional(crossinline usage: ValueUsage<P>): ValueUsage<P> =
        { src, key, previous ->
            Text.of("[", usage(src, key, previous), "]")
        }
}