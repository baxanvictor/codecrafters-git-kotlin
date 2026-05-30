package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import model.Command
import model.Sha1Hex
import utils.isValid

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

    val treeSha = Sha1Hex.new(sha)

    return Command.LsTree(
        commandName = commandName,
        treeSha = treeSha,
        options = Command.LsTree.Options(
            nameOnly = nameOnly
        )
    )
}