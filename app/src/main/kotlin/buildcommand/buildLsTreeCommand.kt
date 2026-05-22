package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import model.Command
import model.Sha1Hex
import model.exception.InvalidSha1Exception
import utils.isValid

fun buildLsTreeCommand(
    args: Array<String>,
    argsParser: ArgParser,
    commandName: String
): Command {
    val options = argsParser.run {
        val nameOnly by option(
            type = ArgType.Boolean,
            fullName = "name-only",
            description = "Flag to only display the names of each tree leaf"
        ).default(false)

        val sha by argument(
            type = ArgType.String,
            description = "GitTree sha hash"
        )

        parse(args)

        val treeSha = Sha1Hex(value = sha)
        if (!treeSha.isValid()) {
            throw InvalidSha1Exception(sha)
        }

        Command.LsTree.Options(
            nameOnly = nameOnly,
            treeSha = treeSha
        )
    }

    return Command.LsTree(
        commandName = commandName,
        options = options
    )
}