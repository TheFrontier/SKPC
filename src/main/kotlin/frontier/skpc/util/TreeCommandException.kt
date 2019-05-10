package frontier.skpc.util

import frontier.ske.text.joinWith
import frontier.ske.text.red
import frontier.ske.text.yellow
import frontier.skpc.CommandTree
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

fun CommandException.wrap(src: CommandSource, tree: CommandTree<*>): CommandException =
    TreeCommandException(this, src, tree)

class TreeCommandException(wrapped: CommandException,
                           private val src: CommandSource,
                           private val tree: CommandTree<*>) : CommandException(wrapped.text ?: Text.EMPTY, false) {

    override fun getText(): Text? {
        return Text.of(
            TextColors.RED, "Exception from ", TextColors.YELLOW, "/", rootAlias(tree), "\n",
            TextColors.RED, super.getText() ?: Text.EMPTY, "\n\n",
            TextColors.RED, "Usage: ", TextColors.YELLOW, "/", usageToRoot(src, tree), "\n",
            TextColors.RED, "Subcommands: ", listSubcommands(tree)
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

    private fun listSubcommands(tree: CommandTree<*>): Text {
        return tree.children.keys.map { it.yellow() }.joinWith(", ".red())
    }
}