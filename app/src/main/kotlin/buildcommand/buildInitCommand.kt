package buildcommand

import model.Command

fun buildInitCommand(
    args: Array<String>,
    commandName: String
): Command {
    return Command.Init(
        commandName = commandName
    )
}