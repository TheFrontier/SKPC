package frontier.skpc

import org.spongepowered.api.command.CommandCallable
import org.spongepowered.api.command.CommandPermissionException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.command.args.parsing.InputTokenizer
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

class RootCallable(
    private val root: CommandTree.Root,
    private val tokenizer: InputTokenizer = InputTokenizer.quotedStrings(false)
) : CommandCallable {

    override fun testPermission(source: CommandSource): Boolean {
        return root.aliases.permission == null || source.hasPermission(root.aliases.permission)
    }

    override fun process(source: CommandSource, arguments: String): CommandResult {
        if (!testPermission(source)) {
            throw CommandPermissionException()
        }

        val args = CommandArgs(arguments, tokenizer.tokenize(arguments, false))
        return root.traverse(source, args, source)
    }

    override fun getSuggestions(source: CommandSource, arguments: String,
                                targetPosition: Location<World>?): List<String> {
        val args = CommandArgs(arguments, tokenizer.tokenize(arguments, true))
        return root.complete(source, args, source)
    }

    override fun getShortDescription(source: CommandSource): Optional<Text> {
        return Optional.empty()
    }

    override fun getHelp(source: CommandSource): Optional<Text> {
        return Optional.empty()
    }

    override fun getUsage(source: CommandSource): Text {
        return root.getShallowUsage(source)
    }
}