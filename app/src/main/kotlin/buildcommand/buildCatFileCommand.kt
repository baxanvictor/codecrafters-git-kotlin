package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import model.Command
import model.Sha1Hex

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

        Command.CatFile.Options(
            sha = Sha1Hex.new(
                sha = prettyPrintSha1 ?: throw RuntimeException("Missing $commandName pretty print arg value")
            )
        )
    }

    return Command.CatFile(
        commandName = commandName,
        options = options
    )
}

