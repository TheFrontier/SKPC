package frontier.skpc.value.standard

import frontier.skpc.value.ValueUsage
import org.spongepowered.api.text.Text

object ValueUsages {

    val empty: ValueUsage = { _, _ ->
        Text.EMPTY
    }

    val identity: ValueUsage = { _, key ->
        key
    }

    val single: ValueUsage = { _, key ->
        Text.of("<", key, ">")
    }

    val variadic: ValueUsage = { _, key ->
        Text.of("<", key, "...>")
    }

    inline fun optional(crossinline usage: ValueUsage): ValueUsage =
        { src, key ->
            Text.of("[", usage(src, key), "]")
        }
}