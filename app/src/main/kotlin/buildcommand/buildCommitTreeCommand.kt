package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import model.Command
import model.Sha1Hex
import model.exception.InvalidSha1Exception
import utils.isValid

fun buildCommitTreeCommand(
    args: Array<String>,
    argsParser: ArgParser,
    commandName: String
): Command {
    val treeShaValue by argsParser.argument(
        type = ArgType.String,
        description = "Git tree sha to commit"
    )

    val parentShaValue by argsParser.option(
        type = ArgType.String,
        shortName = "p",
        description = "Optional parent tree sha"
    )

    val message by argsParser.option(
        type = ArgType.String,
        shortName = "m",
        description = "Optional commit message"
    )

    argsParser.parse(args)

    val treeSha = Sha1Hex(treeShaValue)
    if (!treeSha.isValid()) {
        throw InvalidSha1Exception(treeShaValue)
    }

    val parentSha = parentShaValue?.let { Sha1Hex(it) }
    if (parentSha != null && !parentSha.isValid()) {
        throw InvalidSha1Exception(parentSha.value)
    }

    return Command.CommitTree(
        commandName = commandName,
        treeSha = treeSha,
        options = Command.CommitTree.Options(
            parentSha = parentSha,
            message = message
        )
    )
}