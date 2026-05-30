package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import model.Command
import model.Sha1Hex

fun buildLsTreeCommand(
    args: Array<String>,
    argsParser: ArgParser,
    commandName: String
): Command {
    val nameOnly by argsParser.option(
        type = ArgType.Boolean,
        fullName = "name-only",
        description = "Flag to only display the names of each tree leaf"
    ).default(false)

    val sha by argsParser.argument(
        type = ArgType.String,
        description = "GitTree sha hash"
    )

    argsParser.parse(args)

    return Command.LsTree(
        commandName = commandName,
        treeSha = Sha1Hex.new(sha),
        options = Command.LsTree.Options(
            nameOnly = nameOnly
        )
    )
}