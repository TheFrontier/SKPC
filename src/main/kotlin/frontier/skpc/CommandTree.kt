package frontier.skpc

import frontier.ske.java.util.unwrap
import frontier.ske.text.joinWith
import frontier.ske.text.not
import frontier.ske.text.plus
import frontier.skpc.util.RTuple
import frontier.skpc.util.withUsage
import frontier.skpc.value.Parameter
import org.spongepowered.api.command.CommandCallable
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.ArgumentParseException
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.text.Text

typealias CommandValueExecutor<T> = (T) -> CommandResult

sealed class CommandTree<T> {

    class Root(val aliases: Aliases) : CommandTree<CommandSource>() {
        constructor(vararg aliases: String, permission: String? = null) :
                this(Aliases(*aliases, permission = permission))

        constructor(aliases: List<String>, permission: String? = null) :
                this(Aliases(aliases, permission = permission))

        fun toCallable(): CommandCallable = RootCallable(this)

        override fun getDeepUsage(src: CommandSource): Text {
            return Text.of()
        }

        fun getShallowUsage(src: CommandSource): Text {
            val builder = Text.builder()

            if (children.isNotEmpty()) {
                builder.append(children.map { (alias, _) -> !alias }.joinWith(!"|"))

                if (arguments.size == 1) {
                    builder.append(!"|")
                }
            }

            if (arguments.size == 1) {
                val sequence = generateSequence<Argument<*, *>>(arguments.first()) {
                    if (it.arguments.size == 1) {
                        it.arguments.first()
                    } else {
                        null
                    }
                }.map { it.parameter.usage(src, it.parameter.key) }

                builder.append(sequence.asIterable().joinWith(!" "))
            }

            return builder.build()
        }
    }

    class Child<T>(val parent: CommandTree<T>, val aliases: Aliases) : CommandTree<T>() {

        override fun getDeepUsage(src: CommandSource): Text {
            val parentUsage = parent.getDeepUsage(src)

            return if (parentUsage.isEmpty) {
                Text.of(aliases.first())
            } else {
                Text.of(parent.getDeepUsage(src), " ", aliases.first())
            }
        }
    }

    class Argument<T, V>(val parent: CommandTree<T>, val parameter: Parameter<T, V>) : CommandTree<RTuple<V, T>>() {

        override fun getDeepUsage(src: CommandSource): Text {
            val parentUsage = parent.getDeepUsage(src)

            return if (parentUsage.isEmpty) {
                Text.of(parameter.usage(src, parameter.key))
            } else {
                Text.of(
                    parent.getDeepUsage(src), " ", parameter.usage(src, parameter.key)
                )
            }
        }
    }

    internal val children = HashMap<String, Child<T>>()
    internal val arguments = ArrayList<Argument<T, in Any?>>()
    internal var executor: CommandValueExecutor<in T>? = null

    fun traverse(src: CommandSource, args: CommandArgs, previous: T): CommandResult {
        val snapshot = args.snapshot

        // First try to resolve a subcommand.

        val alias = args.nextIfPresent().unwrap()
        if (alias != null) {
            val child = children[alias]

            if (child != null) {
                return child.traverse(src, args, previous)
            } else if (arguments.isEmpty() && executor == null) {
                throw args.createError(!"Unknown subcommand: $alias").withUsage(this.getDeepUsage(src))
            }
        }

        args.applySnapshot(snapshot)

        // Otherwise, try to parse an argument.

        val argsIterator = arguments.iterator()
        while (argsIterator.hasNext()) {
            val argument = argsIterator.next()

            val parsed = try {
                argument.parameter.parser(src, args, previous)
            } catch (e: ArgumentParseException) {
                if (argsIterator.hasNext()) {
                    args.applySnapshot(snapshot)
                    continue
                } else {
                    val deep = this.getDeepUsage(src)

                    if (!deep.isEmpty) {
                        throw e.withUsage(deep + " " + argument.parameter.usage(src, argument.parameter.key))
                    } else {
                        throw e.withUsage(argument.parameter.usage(src, argument.parameter.key))
                    }
                }
            }

            return argument.traverse(src, args, RTuple(parsed, previous))
        }

        // If all else fails, try to execute the command.
        if (args.hasNext()) {
            args.next()
            throw args.createError(!"Too many arguments!").withUsage(this.getDeepUsage(src))
        }

        val exec = executor
        if (exec != null) {
            return exec(previous)
        } else {
            throw args.createError(!"No executor found for this subcommand.").withUsage(
                this.getDeepUsage(src)
            )
        }
    }

    fun complete(src: CommandSource, args: CommandArgs, previous: T): List<String> {
        return emptyList()
    }

    abstract fun getDeepUsage(src: CommandSource): Text

    @PublishedApi
    internal fun makeChild(aliases: Aliases): Child<T> {
        val child = Child(this, aliases)
        for (alias in aliases) {
            children[alias] = child
        }
        return child
    }

    @PublishedApi
    internal fun makeChild(alias: String): Child<T> = makeChild(Aliases(alias))

    @PublishedApi
    internal fun makeChild(aliases: List<String>): Child<T> = makeChild(Aliases(aliases))

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <V> makeArgument(parameter: Parameter<T, V>): Argument<T, V> {
        val argument = Argument(this, parameter)
        arguments += argument as Argument<T, Any?>
        return argument
    }

    infix fun execute(executor: CommandValueExecutor<in T>) {
        this.executor = executor
    }

    inline infix fun expand(block: CommandTree<T>.() -> Unit) {
        this.apply(block)
    }

    operator fun Aliases.div(aliases: Aliases): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(aliases)
    }

    operator fun Aliases.div(alias: String): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(alias)
    }

    operator fun Aliases.div(aliases: List<String>): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(aliases)
    }

    operator fun <V> Aliases.div(parameter: Parameter<T, V>): Argument<T, V> {
        return this@CommandTree.makeChild(this@div).makeArgument(parameter)
    }

    infix fun Aliases.execute(executor: CommandValueExecutor<in T>) {
        this@CommandTree.makeChild(this@execute).execute(executor)
    }

    inline infix fun Aliases.expand(block: Child<T>.() -> Unit) {
        this@CommandTree.makeChild(this@expand).apply(block)
    }

    operator fun String.div(aliases: Aliases): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(aliases)
    }

    operator fun String.div(alias: String): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(alias)
    }

    operator fun String.div(aliases: List<String>): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(aliases)
    }

    operator fun <V> String.div(parameter: Parameter<T, V>): Argument<T, V> {
        return this@CommandTree.makeChild(this@div).makeArgument(parameter)
    }

    infix fun String.execute(executor: CommandValueExecutor<in T>) {
        this@CommandTree.makeChild(this@execute).execute(executor)
    }

    inline infix fun String.expand(block: Child<T>.() -> Unit) {
        this@CommandTree.makeChild(this@expand).apply(block)
    }

    operator fun List<String>.div(aliases: Aliases): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(aliases)
    }

    operator fun List<String>.div(alias: String): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(alias)
    }

    operator fun List<String>.div(aliases: List<String>): Child<T> {
        return this@CommandTree.makeChild(this@div).makeChild(aliases)
    }

    operator fun <V> List<String>.div(parameter: Parameter<T, V>): Argument<T, V> {
        return this@CommandTree.makeChild(this@div).makeArgument(parameter)
    }

    infix fun List<String>.execute(executor: CommandValueExecutor<in T>) {
        this@CommandTree.makeChild(this@execute).execute(executor)
    }

    inline infix fun List<String>.expand(block: Child<T>.() -> Unit) {
        this@CommandTree.makeChild(this@expand).apply(block)
    }

    operator fun <V> Parameter<T, V>.div(aliases: Aliases): Child<RTuple<V, T>> {
        return this@CommandTree.makeArgument(this@div).makeChild(aliases)
    }

    operator fun <V> Parameter<T, V>.div(alias: String): Child<RTuple<V, T>> {
        return this@CommandTree.makeArgument(this@div).makeChild(alias)
    }

    operator fun <V> Parameter<T, V>.div(aliases: List<String>): Child<RTuple<V, T>> {
        return this@CommandTree.makeArgument(this@div).makeChild(aliases)
    }

    operator fun <V, W> Parameter<T, V>.div(parameter: Parameter<RTuple<V, T>, W>): Argument<RTuple<V, T>, W> {
        return this@CommandTree.makeArgument(this@div).makeArgument(parameter)
    }

    infix fun <V> Parameter<T, V>.execute(executor: CommandValueExecutor<in RTuple<V, T>>) {
        this@CommandTree.makeArgument(this@execute).execute(executor)
    }

    inline infix fun <V> Parameter<T, V>.expand(block: Argument<T, V>.() -> Unit) {
        this@CommandTree.makeArgument(this@expand).apply(block)
    }
}