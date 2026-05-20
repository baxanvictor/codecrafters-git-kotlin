package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import model.Command
import model.Sha1
import model.exception.InvalidSha1Exception
import utils.isValid

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

        val sha1 = Sha1(
            value = prettyPrintSha1 ?: throw RuntimeException("Missing $commandName pretty print arg value")
        )
        if (!sha1.isValid()) {
            throw InvalidSha1Exception(sha1.value)
        }

        Command.CatFile.Options(sha1 = sha1)
    }

    return Command.CatFile(
        commandName = commandName,
        options = options
    )
}

