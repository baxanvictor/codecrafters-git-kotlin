package buildcommand

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import model.Command

fun buildHashObjectCommand(
    args: Array<String>,
    argsParser: ArgParser,
    commandName: String
): Command {
    val options = argsParser.run {
        val write by option(
            type = ArgType.Boolean,
            shortName = "w",
            description = "Enable write mode"
        ).default(false)

        val filename by argument(
            type = ArgType.String,
            description = "Name of the file to hash"
        )

        parse(args)

        Command.HashObject.Options(
            isWriteEnabled = write,
            filename = filename
        )
    }

    return Command.HashObject(
        commandName = commandName,
        options = options
    )
}