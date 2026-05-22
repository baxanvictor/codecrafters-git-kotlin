package buildcommand

import model.Command

fun buildWriteTreeCommand(commandName: String): Command {
    return Command.WriteTree(
        commandName = commandName
    )
}