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

        override fun getDeepUsage(src: CommandSource, previous: CommandSource): Text {
            return Text.of()
        }
    }

    class Child<T>(val parent: CommandTree<T>, val aliases: Aliases) : CommandTree<T>() {

        override fun getDeepUsage(src: CommandSource, previous: T): Text {
            val parentUsage = parent.getDeepUsage(src, previous)

            return if (parentUsage.isEmpty) {
                Text.of(aliases.first())
            } else {
                Text.of(parent.getDeepUsage(src, previous), " ", aliases.first())
            }
        }
    }

    class Argument<T, V>(val parent: CommandTree<T>, val parameter: Parameter<T, V>) : CommandTree<RTuple<V, T>>() {

        override fun getDeepUsage(src: CommandSource, previous: RTuple<V, T>): Text {
            val parentUsage = parent.getDeepUsage(src, previous.tail)

            return if (parentUsage.isEmpty) {
                Text.of(parameter.usage(src, parameter.key, previous.tail))
            } else {
                Text.of(
                    parent.getDeepUsage(src, previous.tail), " ", parameter.usage(src, parameter.key, previous.tail)
                )
            }
        }
    }

    private val children = HashMap<String, Child<T>>()
    private val arguments = ArrayList<Argument<T, in Any?>>()
    private var executor: CommandValueExecutor<in T>? = null

    fun traverse(src: CommandSource, args: CommandArgs, previous: T): CommandResult {
        val snapshot = args.snapshot

        // First try to resolve a subcommand.

        val alias = args.nextIfPresent().unwrap()
        if (alias != null) {
            val child = children[alias]

            if (child != null) {
                return child.traverse(src, args, previous)
            } else if (arguments.isEmpty() && executor == null) {
                throw args.createError(!"Unknown subcommand: $alias")
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
                    val deep = this.getDeepUsage(src, previous)

                    if (!deep.isEmpty) {
                        throw e.withUsage(deep + " " + argument.parameter.usage(src, argument.parameter.key, previous))
                    } else {
                        throw e.withUsage(argument.parameter.usage(src, argument.parameter.key, previous))
                    }
                }
            }

            return argument.traverse(src, args, RTuple(parsed, previous))
        }

        // If all else fails, try to execute the command.

        if (args.hasNext()) {
            throw args.createError(!"Too many arguments!")
        }

        val exec = executor
        if (exec != null) {
            return exec(previous)
        } else {
            throw args.createError(!"No executor found for this subcommand.")
        }
    }

    fun complete(src: CommandSource, args: CommandArgs, previous: T): List<String> {
        return emptyList()
    }

    fun getShallowUsage(src: CommandSource, previous: T): Text {
        val builder = Text.builder()

        if (children.isNotEmpty()) {
            builder.append(children.map { (alias, _) -> !alias }.joinWith(!"|"))

            if (arguments.isNotEmpty()) {
                builder.append(!"|")
            }
        }

        builder.append(arguments.map { it.parameter.usage(src, it.parameter.key, previous) }.joinWith(!"|"))

        return builder.build()
    }

    abstract fun getDeepUsage(src: CommandSource, previous: T): Text

    internal fun makeChild(aliases: Aliases): Child<T> {
        val child = Child(this, aliases)
        for (alias in aliases) {
            children[alias] = child
        }
        return child
    }

    internal fun makeChild(alias: String): Child<T> = makeChild(Aliases(alias))

    internal fun makeChild(aliases: List<String>): Child<T> = makeChild(Aliases(aliases))

    @Suppress("UNCHECKED_CAST")
    internal fun <V> makeArgument(parameter: Parameter<T, V>): Argument<T, V> {
        val argument = Argument(this, parameter)
        arguments += argument as Argument<T, Any?>
        return argument
    }

    infix fun execute(executor: CommandValueExecutor<in T>) {
        this.executor = executor
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
}