package frontier.skpc.util

import frontier.skpc.CommandTree
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors
import org.spongepowered.api.text.format.TextStyles

fun CommandException.wrap(src: CommandSource, tree: CommandTree<*>): CommandException =
    TreeCommandException(this, src, tree)

class TreeCommandException(wrapped: CommandException,
                           private val src: CommandSource,
                           private val tree: CommandTree<*>) : CommandException(wrapped.text ?: Text.EMPTY, false) {

    override fun getText(): Text? {
        val subcommands = listSubcommands(tree)
            .takeUnless { it.isEmpty }
            ?.let { Text.of(TextColors.RED, "\nSubcommands: ", it) }

        return Text.of(
            Text.of(TextColors.RED, TextStyles.ITALIC, "Exception from "),
            Text.of(TextColors.YELLOW, "/", rootAlias(tree)),
            "\n",
            Text.of(TextColors.RED, super.getText() ?: Text.EMPTY),
            "\n\n",
            Text.of(TextColors.RED, "Usage: "),
            Text.of(TextColors.YELLOW, "/", usageToRoot(src, tree), " ", argumentsUsage(src, tree)),
            subcommands ?: Text.EMPTY
        )
    }

    private fun rootAlias(tree: CommandTree<*>): Text {
        return when (tree) {
            is CommandTree.Root -> Text.of(tree.aliases.first())
            is CommandTree.Child<*> -> rootAlias(tree.parent)
            is CommandTree.Argument<*, *> -> rootAlias(tree.parent)
        }
    }

    private fun usageToRoot(src: CommandSource, tree: CommandTree<*>): Text {
        return when (tree) {
            is CommandTree.Root -> Text.of(tree.aliases.first())
            is CommandTree.Child<*> -> {
                Text.of(usageToRoot(src, tree.parent), " ", tree.aliases.first())
            }
            is CommandTree.Argument<*, *> -> {
                Text.of(usageToRoot(src, tree.parent), " ", tree.parameter.usage(src, tree.parameter.key))
            }
        }
    }

    private fun argumentsUsage(src: CommandSource, tree: CommandTree<*>): Text {
        if (tree.arguments.isEmpty()) {
            return Text.EMPTY
        }

        val sequence = generateSequence<CommandTree.Argument<*, *>>(tree.arguments.firstOrNull()) {
            it.arguments.firstOrNull()
        }.map {
            it.parameter.usage(src, it.parameter.key)
        }

        return Text.joinWith(Text.of(" "), sequence.asIterable())
    }

    private fun listSubcommands(tree: CommandTree<*>): Text {
        return Text.joinWith(Text.of(TextColors.RED, ", "), tree.children.keys.map { Text.of(TextColors.YELLOW, it) })
    }
}