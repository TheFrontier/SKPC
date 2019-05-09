package frontier.skpc.util

import org.spongepowered.api.command.args.ArgumentParseException
import org.spongepowered.api.text.Text
import kotlin.reflect.jvm.isAccessible

private val constructor by lazy {
    ArgumentParseException.WithUsage::class.constructors.first().apply {
        this.isAccessible = true
    }
}

fun ArgumentParseException.withUsage(usage: Text): ArgumentParseException.WithUsage =
    constructor.call(this, usage)