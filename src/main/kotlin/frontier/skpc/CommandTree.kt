package frontier.skpc

import frontier.ske.text.not
import frontier.skpc.util.*
import frontier.skpc.value.Parameter
import frontier.skpc.value.standard.ValueParameters.int
import org.spongepowered.api.command.*
import org.spongepowered.api.command.args.ArgumentParseException
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.command.args.parsing.InputTokenizer
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*
import kotlin.collections.List
import kotlin.collections.arrayListOf
import kotlin.collections.emptyList
import kotlin.collections.hashMapOf
import kotlin.collections.listOf
import kotlin.collections.set
import kotlin.collections.toList

sealed class CommandTree<in T> {

    @Throws(CommandException::class)
    abstract fun process(src: CommandSource, args: CommandArgs, arguments: T)

    abstract fun complete(src: CommandSource, args: CommandArgs, arguments: T): List<String>

    abstract fun getUsage(src: CommandSource, builder: Text.Builder)
}

sealed class CommandBranch<T> : CommandTree<T>() {

    protected val branches = hashMapOf<String, CommandBranch<T>>()
    protected val parameters = arrayListOf<CommandParameter<T, Any?>>()
    private var leaf: CommandLeaf<T>? = null

    override fun process(src: CommandSource, args: CommandArgs, arguments: T) {
        val snapshot = args.snapshot

        // Check for sub commands first.
        val alias = args.next()
        val branch = branches[alias]

        if (branch != null) {
            return branch.process(src, args, arguments)
        }

        if (parameters.isEmpty() && leaf == null) {
            throw args.createError(!"No child command found with the given alias.")
        }

        args.applySnapshot(snapshot)

        // Try to parse parameters next.
        val parameterIterator = parameters.iterator()
        while (parameterIterator.hasNext()) {
            val next = parameterIterator.next()

            val parsed = try {
                next.value.parser(src, args, arguments)
            } catch (e: ArgumentParseException) {
                if (parameterIterator.hasNext()) {
                    args.applySnapshot(snapshot)
                } else {
                    throw e
                }
                continue
            }

            return next.process(src, args, RTuple(parsed, arguments))
        }

        // Finally try to execute the command.
        leaf?.process(src, args, arguments)
            ?: throw args.createError(!"No executor was found for this command.")
    }

    override fun complete(src: CommandSource, args: CommandArgs, arguments: T): List<String> {
        TODO()
    }

    override fun getUsage(src: CommandSource, builder: Text.Builder) {
        TODO()
    }

    operator fun div(aliases: Aliases): CommandChild<T> =
        CommandChild(this, aliases).also { this.branches[aliases.aliases] = it }

    operator fun div(alias: String): CommandChild<T> =
        CommandChild(this, Aliases(listOf(alias))).also { this.branches[alias] = it }

    operator fun div(aliases: List<String>): CommandChild<T> =
        CommandChild(this, Aliases(aliases)).also { this.branches[aliases] = it }

    @Suppress("UNCHECKED_CAST")
    operator fun <V> div(parameter: Parameter<T, V>): CommandParameter<T, V> =
        CommandParameter(this, parameter).also { this.parameters.add(it as CommandParameter<T, Any?>) }

    infix fun execute(executor: (T) -> Unit): CommandLeaf<T> =
        CommandLeaf(this, executor).also { this.leaf = it }

    operator fun Aliases.div(aliases: Aliases): CommandChild<T> =
        this@CommandBranch / this@div / aliases

    operator fun Aliases.div(alias: String): CommandChild<T> =
        this@CommandBranch / this@div / alias

    operator fun Aliases.div(aliases: List<String>): CommandChild<T> =
        this@CommandBranch / this@div / aliases

    operator fun <V> Aliases.div(parameter: Parameter<T, V>): CommandParameter<T, V> =
        this@CommandBranch / this@div / parameter

    infix fun Aliases.execute(executor: (T) -> Unit): CommandLeaf<T> =
        this@CommandBranch / this@execute execute executor

    inline infix fun Aliases.then(init: CommandChild<T>.() -> Unit) =
        (this@CommandBranch / this@then).apply(init)

    operator fun String.div(aliases: Aliases): CommandChild<T> =
        this@CommandBranch / this@div / aliases

    operator fun String.div(alias: String): CommandChild<T> =
        this@CommandBranch / this@div / alias

    operator fun String.div(aliases: List<String>): CommandChild<T> =
        this@CommandBranch / this@div / aliases

    operator fun <V> String.div(parameter: Parameter<T, V>): CommandParameter<T, V> =
        this@CommandBranch / this@div / parameter

    infix fun String.execute(executor: (T) -> Unit): CommandLeaf<T> =
        this@CommandBranch / this@execute execute executor

    inline infix fun String.then(init: CommandChild<T>.() -> Unit) =
        (this@CommandBranch / this@then).apply(init)

    operator fun List<String>.div(aliases: Aliases): CommandChild<T> =
        this@CommandBranch / this@div / aliases

    operator fun List<String>.div(alias: String): CommandChild<T> =
        this@CommandBranch / this@div / alias

    operator fun List<String>.div(aliases: List<String>): CommandChild<T> =
        this@CommandBranch / this@div / aliases

    operator fun <V> List<String>.div(parameter: Parameter<T, V>): CommandParameter<T, V> =
        this@CommandBranch / this@div / parameter

    infix fun List<String>.execute(executor: (T) -> Unit): CommandLeaf<T> =
        this@CommandBranch / this@execute execute executor

    inline infix fun List<String>.then(init: CommandChild<T>.() -> Unit) =
        (this@CommandBranch / this@then).apply(init)

    operator fun <V> Parameter<T, V>.div(aliases: Aliases): CommandChild<RTuple<V, T>> =
        this@CommandBranch / this@div / aliases

    operator fun <V> Parameter<T, V>.div(alias: String): CommandChild<RTuple<V, T>> =
        this@CommandBranch / this@div / alias

    operator fun <V> Parameter<T, V>.div(aliases: List<String>): CommandChild<RTuple<V, T>> =
        this@CommandBranch / this@div / aliases

    @Suppress("UNCHECKED_CAST")
    operator fun <V, W> Parameter<T, V>.div(parameter: Parameter<RTuple<V, T>, W>): CommandParameter<RTuple<V, T>, W> =
        this@CommandBranch / this@div / parameter

    infix fun <V> Parameter<T, V>.execute(executor: (RTuple<V, T>) -> Unit): CommandLeaf<RTuple<V, T>> =
        this@CommandBranch / this@execute execute executor

    inline infix fun <V> Parameter<T, V>.then(init: CommandParameter<T, V>.() -> Unit) =
        (this@CommandBranch / this@then).apply(init)
}

class CommandRoot(val aliases: Aliases) : CommandBranch<CommandSource>(),
    CommandCallable {

    private val tokenizer = InputTokenizer.quotedStrings(false)

    companion object {
        inline operator fun invoke(aliases: List<String>, permission: String? = null,
                                   init: CommandRoot.() -> Unit) =
            CommandRoot(Aliases(aliases, permission)).apply(init)

        inline operator fun invoke(vararg aliases: String, permission: String? = null,
                                   init: CommandRoot.() -> Unit) =
            CommandRoot(Aliases(aliases.toList(), permission)).apply(init)
    }

    override fun process(source: CommandSource, arguments: String): CommandResult {
        if (!testPermission(source)) {
            throw CommandPermissionException()
        }

        val args = CommandArgs(arguments, tokenizer.tokenize(arguments, false))

        process(source, args, source)

        return CommandResult.success()
    }

    override fun testPermission(source: CommandSource): Boolean {
        return aliases.permission != null && source.hasPermission(aliases.permission)
    }

    override fun getSuggestions(source: CommandSource, arguments: String,
                                targetPosition: Location<World>?): List<String> {
        return emptyList()
    }

    override fun getShortDescription(source: CommandSource): Optional<Text> {
        return Optional.empty()
    }

    override fun getHelp(source: CommandSource): Optional<Text> {
        return Optional.empty()
    }

    override fun getUsage(source: CommandSource): Text {
        val builder = Text.builder()

        val aliases = branches.keys.iterator()
        val params = parameters.iterator()

        while (aliases.hasNext()) {
            builder.append(Text.of(aliases.next()))
            if (aliases.hasNext() || params.hasNext()) {
                builder.append(CommandFormatting.PIPE)
            }
        }

        while (params.hasNext()) {
            val next = params.next().value
            val usage = next.usage(source, next.key, source)

            if (!usage.isEmpty) {
                builder.append(usage)
                if (params.hasNext()) {
                    builder.append(CommandFormatting.PIPE)
                }
            }
        }

        return builder.build()
    }
}

class CommandChild<T>(val parent: CommandBranch<in T>, val aliases: Aliases) : CommandBranch<T>() {

    inline infix fun then(init: CommandChild<T>.() -> Unit): CommandChild<T> =
        this.apply(init)
}

class CommandParameter<T, V>(val parent: CommandBranch<in T>, val value: Parameter<T, V>) :
    CommandBranch<RTuple<V, T>>() {

    inline infix fun then(init: CommandParameter<T, V>.() -> Unit): CommandParameter<T, V> =
        this.apply(init)
}

class CommandLeaf<T>(val parent: CommandBranch<in T>, val executor: (T) -> Unit) : CommandTree<T>() {

    override fun process(src: CommandSource, args: CommandArgs, arguments: T) {
        executor(arguments)
    }

    override fun complete(src: CommandSource, args: CommandArgs, arguments: T): List<String> {
        return emptyList()
    }

    override fun getUsage(src: CommandSource, builder: Text.Builder) {}
}

fun test() {
    val commandSum = CommandRoot("sum") {
        int("value1") / int("value2") execute { (value1, value2, source) ->
            source.sendMessage(!"$value1 + $value2 = ${value1 + value2}")
            CommandResult.success()
        }
    }
}