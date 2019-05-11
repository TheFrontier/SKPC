package frontier.skpc

import frontier.skpc.util.CommandFormatting
import frontier.skpc.util.RTuple
import frontier.skpc.value.Parameter
import org.spongepowered.api.command.*
import org.spongepowered.api.command.args.ArgumentParseException
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.text.Text

typealias CommandValueExecutor<Source, Value> = (Source, Value) -> CommandResult

sealed class CommandTree<T> {

    class Root(val aliases: Aliases) : CommandTree<Unit>() {
        constructor(vararg aliases: String, permission: String? = null) :
                this(Aliases(*aliases, permission = permission))

        constructor(aliases: List<String>, permission: String? = null) :
                this(Aliases(aliases, permission = permission))

        fun toCallable(): CommandCallable = RootCallable(this)

        fun getShallowUsage(src: CommandSource): Text {
            val builder = Text.builder()

            if (children.isNotEmpty()) {
                builder.append(Text.joinWith(CommandFormatting.PIPE, children.map { (alias, _) -> Text.of(alias) }))

                if (arguments.size == 1) {
                    builder.append(CommandFormatting.PIPE)
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

                builder.append(Text.joinWith(CommandFormatting.SPACE, sequence.asIterable()))
            }

            return builder.build()
        }
    }

    class Child<T>(val parent: CommandTree<T>, val aliases: Aliases) : CommandTree<T>()

    class Argument<T, V>(val parent: CommandTree<T>, val parameter: Parameter<T, V>) : CommandTree<RTuple<V, T>>()

    internal val children = HashMap<String, Child<T>>()
    internal val arguments = ArrayList<Argument<T, in Any?>>()
    internal var executor: CommandValueExecutor<in CommandSource, in T>? = null

    @Throws(CommandException::class)
    fun traverse(src: CommandSource, args: CommandArgs, previous: T): CommandResult {
        if (!args.hasNext()) {
            val exec = executor

            if (exec != null) {
                return exec(src, previous)
            } else if (children.isNotEmpty() || arguments.isNotEmpty()) {
                throw args.createError(Text.of("Not enough arguments!")).wrap(src, this)
            } else {
                when (this) {
                    is Root -> throw CommandException(Text.of("This command has no executor."))
                    is Child -> throw CommandException(Text.of("Could not find any executor for this subcommand."))
                        .wrap(src, this.parent)
                    is Argument<*, *> -> throw CommandException(
                        Text.of("Could not find any executor for this subcommand.")
                    )
                        .wrap(src, this.parent)
                }
            }
        }

        val snapshot = args.snapshot

        // Search for a child command by alias.
        val alias = args.next()
        val child = children[alias]

        if (child != null) {
            // Check if the source can use this child command.
            if (child.aliases.permission != null && !src.hasPermission(child.aliases.permission)) {
                throw CommandPermissionException()
            }

            // Found a child command; traverse it's subtree.
            return child.traverse(src, args, previous)
        } else {
            // Failed to find a child; rollback the CommandArgs.
            args.applySnapshot(snapshot)
        }

        // Now try to parse any of the arguments.
        var lastError: ArgumentParseException? = null

        for (argument in arguments) {
            try {
                val parsed = argument.parameter.parser(src, args, previous)

                // Successfully parsed the argument; traverse it's subtree.
                return argument.traverse(src, args, RTuple(parsed, previous))
            } catch (e: ArgumentParseException) {
                // Failed to parse the argument; rollback the CommandArgs and try the next argument.
                lastError = e
                args.applySnapshot(snapshot)
            }
        }

        if (lastError != null) {
            throw lastError.wrap(src, this)
        } else {
            throw args.createError(Text.of("Failed to parse this subcommand.")).wrap(src, this)
        }
    }

    @Throws(CommandException::class)
    fun complete(src: CommandSource, args: CommandArgs, previous: T): List<String> {
        val completions = HashSet<String>()

        completions += children.keys

        for (argument in arguments) {
            completions += argument.parameter.completer(src, args, previous)
        }

        if (args.hasNext()) {
            val snapshot = args.snapshot

            // CommandArgs still remaining... search for a child command by alias.
            val alias = args.next()
            val child = children[alias]
            if (child != null) {
                // Found a child command; complete it's subtree.
                return child.complete(src, args, previous)
            } else {
                // Failed to find a child; rollback the CommandArgs.
                args.applySnapshot(snapshot)
            }

            // Now try to parse then complete any of the arguments.
            for (argument in arguments) {
                try {
                    val parsed = argument.parameter.parser(src, args, previous)

                    when {
                        snapshot == args.snapshot -> {
                            // No CommandArgs were consumed.
                            args.applySnapshot(snapshot)
                        }
                        args.hasNext() -> {
                            // Parsed argument with CommandArgs remaining; complete it's subtree.
                            return argument.complete(src, args, RTuple(parsed, previous))
                        }
                        else -> {
                            // Fully parsed the argument with no remaining CommandArgs.
                            args.applySnapshot(snapshot)
                        }
                    }
                } catch (e: ArgumentParseException) {
                    // Failed to parse the argument; rollback the CommandArgs and try the next argument.
                    args.applySnapshot(snapshot)
                }
            }
        }

        return completions.toList()
    }

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

    operator fun div(aliases: Aliases): Child<T> {
        return this@CommandTree.makeChild(aliases)
    }

    operator fun div(alias: String): Child<T> {
        return this@CommandTree.makeChild(alias)
    }

    operator fun div(aliases: List<String>): Child<T> {
        return this@CommandTree.makeChild(aliases)
    }

    operator fun <V> div(parameter: Parameter<T, V>): Argument<T, V> {
        return this@CommandTree.makeArgument(parameter)
    }

    infix fun execute(executor: CommandValueExecutor<in CommandSource, in T>) {
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

    infix fun Aliases.execute(executor: CommandValueExecutor<in CommandSource, in T>) {
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

    infix fun String.execute(executor: CommandValueExecutor<in CommandSource, in T>) {
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

    infix fun List<String>.execute(executor: CommandValueExecutor<in CommandSource, in T>) {
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

    infix fun <V> Parameter<T, V>.execute(executor: CommandValueExecutor<in CommandSource, in RTuple<V, T>>) {
        this@CommandTree.makeArgument(this@execute).execute(executor)
    }

    inline infix fun <V> Parameter<T, V>.expand(block: Argument<T, V>.() -> Unit) {
        this@CommandTree.makeArgument(this@expand).apply(block)
    }
}