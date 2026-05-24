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
    val write by argsParser.option(
        type = ArgType.Boolean,
        shortName = "w",
        description = "Enable write mode"
    ).default(false)

    val filename by argsParser.argument(
        type = ArgType.String,
        description = "Name of the file to hash"
    )

    argsParser.parse(args)

    return Command.HashObject(
        commandName = commandName,
        filename = filename,
        options = Command.HashObject.Options(
            isWriteEnabled = write,
        )
    )
}