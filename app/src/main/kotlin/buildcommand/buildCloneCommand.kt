package buildcommand

import io.ktor.http.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import model.Command

fun buildCloneCommand(
    args: Array<String>,
    argsParser: ArgParser,
    commandName: String
) : Command {
    val repoUrl by argsParser.argument(
        type = ArgType.String,
        description = "Repo url to clone"
    )

    val targetDir by argsParser.argument(
        type = ArgType.String,
        description = "Dir to clone the repo into"
    )

    argsParser.parse(args)

    parseUrl(repoUrl)
        ?: throw RuntimeException("Invalid repo url: $repoUrl")

    return Command.Clone(
        commandName = commandName,
        repoUrl = repoUrl,
        targetDir = targetDir
    )
}