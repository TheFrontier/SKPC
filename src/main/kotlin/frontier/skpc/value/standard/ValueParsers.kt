package frontier.skpc.value.standard

import frontier.ske.gameRegistry
import frontier.ske.getType
import frontier.ske.java.lang.*
import frontier.ske.java.util.unwrap
import frontier.ske.plugin.toPluginContainer
import frontier.ske.service.require
import frontier.ske.serviceManager
import frontier.ske.text.not
import frontier.skpc.value.IValueParser
import frontier.skpc.value.ValueParser
import frontier.skpc.value.parser
import org.spongepowered.api.CatalogType
import org.spongepowered.api.command.CommandPermissionException
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.ArgumentParseException
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.service.permission.PermissionService
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.api.service.permission.SubjectCollection
import org.spongepowered.api.world.World
import org.spongepowered.api.world.storage.WorldProperties
import java.math.BigDecimal
import java.math.BigInteger
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.util.*

object ValueParsers {

    /**
     * Requires an argument to be a string. Any provided argument will fit under this argument.
     *
     * Gives a value of type [String].
     */
    val string: ValueParser<Any?, String> = { _, args, _ ->
        args.next()
    }

    /**
     * Requires an argument to be an integer (base 10).
     *
     * Gives a value of type [Int].
     */
    val int: ValueParser<Any?, Int> = boundedValue(String::toIntOrNull, Int.MIN_VALUE..Int.MAX_VALUE)

    /**
     * Requires an argument to be a long (base 10).
     *
     * Gives a value of type [Long].
     */
    val long: ValueParser<Any?, Long> = boundedValue(String::toLongOrNull, Long.MIN_VALUE..Long.MAX_VALUE)

    /**
     * Requires an argument to be a single-precision floating point number.
     *
     * Gives a value of type [Float]
     */
    val float: ValueParser<Any?, Float> = boundedValue(String::toFloatOrNull, Float.MIN_VALUE..Float.MAX_VALUE)

    /**
     * Requires an argument to be a double-precision floating point number.
     *
     * Gives a value of type [Double].
     */
    val double: ValueParser<Any?, Double> = boundedValue(String::toDoubleOrNull, Double.MIN_VALUE..Double.MAX_VALUE)

    /**
     * Requires an argument to be a boolean.
     *
     * Values recognized as truthful:
     * - true
     * - t
     * - yes
     * - y
     * - 1
     *
     * Values recognized as falsehoods:
     * - false
     * - f
     * - no
     * - n
     * - 0
     *
     * Gives a value of type [Boolean].
     */
    val boolean: ValueParser<Any?, Boolean> = choices(
        mapOf(
            "true" to true,
            "t" to true,
            "yes" to true,
            "y" to true,
            "1" to true,
            "false" to false,
            "f" to false,
            "no" to false,
            "n" to false,
            "0" to false
        )
    )

    /**
     * Requires an argument to represent a url.
     *
     * Gives a value of type [URL].
     */
    val url: ValueParser<Any?, URL> = { _, args, _ ->
        val arg = args.next()
        val url = try {
            URL(arg)
        } catch (e: MalformedURLException) {
            throw ArgumentParseException(!"Invalid url", e, arg, 0)
        }
        try {
            url.toURI()
        } catch (e: URISyntaxException) {
            throw ArgumentParseException(!"Invalid url", e, arg, 0)
        }
        url
    }

    /**
     * Requires an argument to represent a UUID.
     *
     * Gives a value of type [UUID].
     */
    val uuid: ValueParser<Any?, UUID> = { _, args, _ ->
        try {
            args.next().toUUID()
        } catch (e: IllegalArgumentException) {
            throw args.createError(!"Invalid UUID")
        }
    }

    /**
     * Requires an argument to represent an arbitrarily large number.
     *
     * Gives a value of type [BigDecimal].
     */
    val bigDecimal: ValueParser<Any?, BigDecimal> = { _, args, _ ->
        val arg = args.next()
        try {
            BigDecimal(arg)
        } catch (e: NumberFormatException) {
            throw args.createError(!"Expected a number, but input '$arg' was not")
        }
    }

    /**
     * Requires an argument to represent an arbitrarily large integer.
     *
     * Gives a value of type [BigInteger].
     */
    val bigInteger: ValueParser<Any?, BigInteger> = { _, args, _ ->
        val arg = args.next()
        try {
            BigInteger(arg)
        } catch (e: NumberFormatException) {
            throw args.createError(!"Expected an integer, but input '$arg' was not")
        }
    }

    /**
     * The parser to fetch the command source.
     *
     * Gives a value of type [CommandSource].
     */
    val commandSource: ValueParser<Any?, CommandSource> = { src, _, _ ->
        src
    }

    /**
     * Requires an argument to be the name of an online player.
     *
     * Gives a value of type [Player].
     */
    val player: ValueParser<Any?, Player> = { _, args, _ ->
        val name = args.next()
        name.toPlayer()
            ?: throw args.createError(!"Player '$name' was not found")
    }

    /**
     * Requires an argument to be the name of an online player,
     * or if no player is found and the source is a [Player], pass the source instead.
     *
     * Gives a value of type [Player].
     */
    val playerOrSource: ValueParser<Any?, Player> = orSource(player)

    /**
     * Requires an argument to represent players who have been online at some point.
     *
     * Gives a value of type [User].
     */
    val user: ValueParser<Any?, User> = { _, args, _ ->
        val name = args.next()
        name.toUser()
            ?: throw args.createError(!"User '$name' was not found")
    }

    /**
     * Requires an argument to be a loaded world.
     *
     * Gives a value of type [World].
     */
    val world: ValueParser<Any?, World> = { _, args, _ ->
        val name = args.next()
        name.toWorld()
            ?: throw args.createError(!"Loaded World '$name' was not found")
    }

    /**
     * Requires an argument to be a loaded or unloaded world.
     *
     * Gives a value of type [WorldProperties].
     */
    val worldProperties: ValueParser<Any?, WorldProperties> = { _, args, _ ->
        val name = args.next()
        name.toWorldProperties()
            ?: throw args.createError(!"World '$name' was not found")
    }

    /**
     * Requires an argument to represent a plugin's id.
     *
     * Gives a value of type [PluginContainer].
     */
    val plugin: ValueParser<Any?, PluginContainer> = { _, args, _ ->
        val plugin = args.next()
        plugin.toPluginContainer()
            ?: throw args.createError(!"Plugin '$plugin' was not found")
    }

    /**
     * Requires an argument to be an for a member of the specified catalog type.
     *
     * Gives a value of type [T].
     */
    inline fun <reified T : CatalogType> catalogType(): ValueParser<Any?, T> = { _, args, _ ->
        val id = args.next()
        gameRegistry.getType(id)
            ?: throw args.createError(!"${T::class.simpleName} '$id' was not found")
    }

    /**
     * Requires an argument to be a name under the provided enum.
     *
     * Gives a value of type [T].
     */
    inline fun <reified T : Enum<T>> enum(): ValueParser<Any?, T> {
        val values = T::class.java.enumConstants.associateBy { it.name.toLowerCase() }

        return { _, args, _ ->
            val value = args.next().toLowerCase()
            values[value]
                ?: throw args.createError(!"${T::class.simpleName} '$value' was not found")
        }
    }

    /**
     * Requires an argument to be the identifier for a subject collection.
     *
     * Gives a value of type [SubjectCollection].
     */
    val subjectCollection: ValueParser<Any?, SubjectCollection> = { _, args, _ ->
        val collection = args.next()
        serviceManager.require<PermissionService>().getCollection(collection).orElseThrow {
            args.createError(!"Could not find any subject collection named '$collection'")
        }
    }

    /**
     * Requires an argument to be the identifier for a subject,
     * and its preceding argument to be the identifier for a subject collection.
     *
     * Gives a value of type [Subject], requiring a preceding value of type [SubjectCollection].
     */
    val subject = parser { _, args, collection: SubjectCollection ->
        val subject = args.next()
        collection.getSubject(subject).unwrap()
            ?: throw args.createError(!"Could not find any ${collection.identifier} subject named '$subject'")
    }

    fun subjectOf(collectionIdentifier: String): ValueParser<Any?, Subject> {
        val collection = serviceManager.require<PermissionService>().getCollection(collectionIdentifier).unwrap()

        return { _, args, _ ->
            val subject = args.next()
            collection?.getSubject(subject)?.unwrap()
                ?: throw args.createError(!"Could not find any $collectionIdentifier subject named '$subject'")
        }
    }

    inline fun <P, T> choices(crossinline keys: () -> Collection<String>,
                              crossinline get: (key: String, previous: P) -> T?) =
        IValueParser<P, T> { _, args, previous ->
            get(args.next(), previous)
                ?: throw args.createError(!"Argument was not a valid choice.\nValid choices: ${keys().joinToString()}")
        }

    fun <T> choices(map: Map<String, T>): ValueParser<Any?, T> =
        ValueParsers.choices(map::keys) { key, _ -> map[key] }

    inline fun <P, reified T : Comparable<T>> bounded(crossinline parser: ValueParser<P, T>,
                                                      range: ClosedRange<T>): ValueParser<P, T> =
        { src, args, previous ->
            val parsed = parser(src, args, previous)
            if (parsed !in range) {
                throw args.createError(!"Input must be between ${range.start} and ${range.endInclusive}")
            }
            parsed
        }

    inline fun <reified T : Comparable<T>> boundedValue(crossinline parse: (String) -> T?,
                                                        range: ClosedRange<T>) =
        parser { _, args ->
            parse(args.next())?.takeIf { it in range }
                ?: throw args.createError(
                    !"Expected a ${T::class.simpleName} between ${range.start} and ${range.endInclusive}"
                )
        }

    inline fun <P, reified T : CommandSource> orSource(crossinline parser: ValueParser<P, T>): ValueParser<P, T> =
        { src, args, previous ->
            val snapshot = args.snapshot
            try {
                parser(src, args, previous)
            } catch (e: ArgumentParseException) {
                if (src is T) {
                    args.applySnapshot(snapshot)
                    src
                } else {
                    throw e
                }
            }
        }

    inline fun <P, T> optional(crossinline parser: ValueParser<P, T>,
                               weak: Boolean = false): ValueParser<P, T?> =
        { src, args, previous ->
            if (!args.hasNext()) {
                null
            } else {
                val snapshot = args.snapshot
                try {
                    parser(src, args, previous)
                } catch (e: ArgumentParseException) {
                    if (weak || args.hasNext()) {
                        args.applySnapshot(snapshot)
                        null
                    } else {
                        throw e
                    }
                }
            }
        }

    inline fun <P, T> optionalOr(crossinline parser: ValueParser<P, T>,
                                 default: T, weak: Boolean = false): ValueParser<P, T> =
        { src, args, previous ->
            if (!args.hasNext()) {
                default
            } else {
                val snapshot = args.snapshot
                try {
                    parser(src, args, previous)
                } catch (e: ArgumentParseException) {
                    if (weak || args.hasNext()) {
                        args.applySnapshot(snapshot)
                        default
                    } else {
                        throw e
                    }
                }
            }
        }

    inline fun <P, T> remaining(crossinline parser: ValueParser<P, T>): ValueParser<P, List<T>> =
        { src, args, previous ->
            val values = arrayListOf<T>()
            while (args.hasNext()) {
                values += parser(src, args, previous)
            }
            values
        }

    inline fun <P, T> repeated(count: Int, crossinline parser: ValueParser<P, T>): ValueParser<P, List<T>> =
        { src, args, previous ->
            val values = arrayListOf<T>()
            for (i in 1..count) {
                values += parser(src, args, previous)
            }
            values
        }

    inline fun <P, T> permission(permission: String, crossinline parser: ValueParser<P, T>): ValueParser<P, T> =
        { src, args, previous ->
            if (!src.hasPermission(permission)) {
                throw CommandPermissionException()
            }
            parser(src, args, previous)
        }
}

inline fun <P, reified T : CommandSource> ValueParser<P, T>.orSource(): ValueParser<P, T> =
    ValueParsers.orSource(this)

inline fun <P, reified T> ValueParser<P, T>.optional(weak: Boolean = false): ValueParser<P, T?> =
    ValueParsers.optional(this, weak)

inline fun <P, reified T> ValueParser<P, T>.optionalOr(default: T, weak: Boolean = false): ValueParser<P, T> =
    ValueParsers.optionalOr(this, default, weak)

inline fun <P, reified T> ValueParser<P, T>.remaining(): ValueParser<P, List<T>> =
    ValueParsers.remaining(this)

inline fun <P, reified T> ValueParser<P, T>.repeated(count: Int): ValueParser<P, List<T>> =
    ValueParsers.repeated(count, this)

inline fun <P, reified T> ValueParser<P, T>.permission(permission: String): ValueParser<P, T> =
    ValueParsers.permission(permission, this)
