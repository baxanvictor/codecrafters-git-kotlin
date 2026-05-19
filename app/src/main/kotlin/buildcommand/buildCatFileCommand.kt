package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import model.Command
import model.Sha1

fun buildCatFileCommand(
    args: Array<String>,
    argsParser: ArgParser,
    commandName: String
): Command {
    val options = argsParser.run {
        val prettyPrintSha1 by option(
            type = ArgType.String,
            shortName = "p"
        )

        parse(args)

        val sha1 = prettyPrintSha1 ?: throw RuntimeException("Missing $commandName pretty print arg value")

        listOf(
            Command.CatFile.Option.PrettyPrint(
                sha1 = Sha1(sha1)
            )
        )
    }

    return Command.CatFile(
        commandName = commandName,
        options = options
    )
}

