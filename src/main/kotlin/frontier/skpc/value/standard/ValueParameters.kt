package frontier.skpc.value.standard

import frontier.skpc.util.RTuple
import frontier.skpc.value.ValueParameter
import org.spongepowered.api.CatalogType
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.api.service.permission.SubjectCollection
import org.spongepowered.api.world.World
import org.spongepowered.api.world.storage.WorldProperties
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.util.*

object ValueParameters {

    /**
     * Requires an argument to be a string. Any provided argument will fit under this argument.
     *
     * Gives a value of type [String].
     */
    val string: ValueParameter<Any?, String> = ValueParameter(
        ValueParsers.string,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * Requires an argument to be an integer (base 10).
     *
     * Gives a value of type [Int].
     */
    val int: ValueParameter<Any?, Int> = ValueParameter(
        ValueParsers.int,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * Requires an argument to be a long (base 10).
     *
     * Gives a value of type [Long].
     */
    val long: ValueParameter<Any?, Long> = ValueParameter(
        ValueParsers.long,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * Requires an argument to be a single-precision floating point number.
     *
     * Gives a value of type [Float]
     */
    val float: ValueParameter<Any?, Float> = ValueParameter(
        ValueParsers.float,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * Requires an argument to be a double-precision floating point number.
     *
     * Gives a value of type [Double].
     */
    val double: ValueParameter<Any?, Double> = ValueParameter(
        ValueParsers.double,
        ValueCompleters.empty,
        ValueUsages.single
    )

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
    val boolean: ValueParameter<Any?, Boolean> = ValueParameter(
        ValueParsers.boolean,
        ValueCompleters.boolean,
        ValueUsages.single
    )

    /**
     * Requires an argument to represent a url.
     *
     * Gives a value of type [URL].
     */
    val url: ValueParameter<Any?, URL> = ValueParameter(
        ValueParsers.url,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * Requires an argument to represent a UUID.
     *
     * Gives a value of type [UUID].
     */
    val uuid: ValueParameter<Any?, UUID> = ValueParameter(
        ValueParsers.uuid,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * Requires an argument to represent an arbitrarily large number.
     *
     * Gives a value of type [BigDecimal].
     */
    val bigDecimal: ValueParameter<Any?, BigDecimal> = ValueParameter(
        ValueParsers.bigDecimal,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * Requires an argument to represent an arbitrarily large integer.
     *
     * Gives a value of type [BigInteger].
     */
    val bigInteger: ValueParameter<Any?, BigInteger> = ValueParameter(
        ValueParsers.bigInteger,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * The parser to fetch the command source.
     *
     * Gives a value of type [CommandSource].
     */
    val commandSource: ValueParameter<Any?, CommandSource> = ValueParameter(
        ValueParsers.commandSource,
        ValueCompleters.empty,
        ValueUsages.empty
    )

    /**
     * Requires an argument to be the name of an online player.
     *
     * Gives a value of type [Player].
     */
    val player: ValueParameter<Any?, Player> = ValueParameter(
        ValueParsers.player,
        ValueCompleters.player,
        ValueUsages.single
    )

    /**
     * Requires an argument to be the name of an online player,
     * or if no player is found and the source is a [Player], pass the source instead.
     *
     * Gives a value of type [Player].
     */
    val playerOrSource: ValueParameter<Any?, Player> = ValueParameter(
        ValueParsers.playerOrSource,
        ValueCompleters.player,
        ValueUsages.single
    )

    /**
     * Requires an argument to represent players who have been online at some point.
     *
     * Gives a value of type [User].
     */
    val user: ValueParameter<Any?, User> = ValueParameter(
        ValueParsers.user,
        ValueCompleters.user,
        ValueUsages.single
    )

    /**
     * Requires an argument to be a loaded world.
     *
     * Gives a value of type [World].
     */
    val world: ValueParameter<Any?, World> = ValueParameter(
        ValueParsers.world,
        ValueCompleters.world,
        ValueUsages.single
    )

    /**
     * Requires an argument to be a loaded or unloaded world.
     *
     * Gives a value of type [WorldProperties].
     */
    val worldProperties: ValueParameter<Any?, WorldProperties> = ValueParameter(
        ValueParsers.worldProperties,
        ValueCompleters.worldProperties,
        ValueUsages.single
    )

    /**
     * Requires an argument to represent a plugin's id.
     *
     * Gives a value of type [PluginContainer].
     */
    val plugin: ValueParameter<Any?, PluginContainer> = ValueParameter(
        ValueParsers.plugin,
        ValueCompleters.plugin,
        ValueUsages.single
    )

    /**
     * Requires an argument to be an for a member of the specified catalog type.
     *
     * Gives a value of type [T].
     */
    inline fun <reified T : CatalogType> catalogType(): ValueParameter<Any?, T> = ValueParameter(
        ValueParsers.catalogType(),
        ValueCompleters.catalogType<T>(),
        ValueUsages.single
    )

    /**
     * Requires an argument to be a name under the provided enum.
     *
     * Gives a value of type [T].
     */
    inline fun <reified T : Enum<T>> enum(): ValueParameter<Any?, T> = ValueParameter(
        ValueParsers.enum(),
        ValueCompleters.enum<T>(),
        ValueUsages.single
    )

    /**
     * Requires an argument to be the identifier for a subject collection.
     *
     * Gives a value of type [SubjectCollection].
     */
    val subjectCollection: ValueParameter<Any?, SubjectCollection> = ValueParameter(
        ValueParsers.subjectCollection,
        ValueCompleters.empty,
        ValueUsages.single
    )

    /**
     * Requires an argument to be the identifier for a subject,
     * and its preceding argument to be the identifier for a subject collection.
     *
     * Gives a value of type [Subject], requiring a preceding value of type [SubjectCollection].
     */
    val subject: ValueParameter<RTuple<SubjectCollection, *>, Subject> = ValueParameter(
        ValueParsers.subject,
        ValueCompleters.empty,
        ValueUsages.single
    )

    fun subjectOf(collectionIdentifier: String): ValueParameter<Any?, Subject> = ValueParameter(
        ValueParsers.subjectOf(collectionIdentifier),
        ValueCompleters.subject(collectionIdentifier),
        ValueUsages.single
    )
}

inline fun <P, reified T> ValueParameter<P, T>.optional(weak: Boolean = false): ValueParameter<P, T?> =
    ValueParameter(
        this.parser.optional(weak),
        this.completer,
        ValueUsages.optional(this.usage)
    )

inline fun <P, reified T> ValueParameter<P, T>.optionalOr(default: T, weak: Boolean = false): ValueParameter<P, T> =
    ValueParameter(
        this.parser.optionalOr(default, weak),
        this.completer,
        ValueUsages.optional(this.usage)
    )

inline fun <P, reified T> ValueParameter<P, T>.remaining(): ValueParameter<P, List<T>> =
    ValueParameter(
        this.parser.remaining(),
        ValueCompleters.remaining(this.completer, this.parser),
        ValueUsages.variadic
    )

inline fun <P, reified T> ValueParameter<P, T>.repeated(count: Int): ValueParameter<P, List<T>> =
    ValueParameter(
        this.parser.repeated(count),
        ValueCompleters.repeated(this.completer, this.parser, count),
        ValueUsages.variadic
    )

inline fun <P, reified T> ValueParameter<P, T>.permission(permission: String): ValueParameter<P, T> =
    ValueParameter(
        this.parser.permission(permission),
        this.completer,
        this.usage
    )