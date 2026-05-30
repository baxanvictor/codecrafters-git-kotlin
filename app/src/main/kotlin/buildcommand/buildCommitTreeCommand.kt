package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import model.Command
import model.Sha1Hex

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

    val treeSha = Sha1Hex.new(treeShaValue)
    val parentSha = Sha1Hex.new(
        sha = parentShaValue ?: throw RuntimeException("Missing $commandName parent sha arg value")
    )

    return Command.CommitTree(
        commandName = commandName,
        treeSha = treeSha,
        options = Command.CommitTree.Options(
            parentSha = parentSha,
            message = message
        )
    )
}