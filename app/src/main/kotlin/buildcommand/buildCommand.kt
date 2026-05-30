package buildcommand

import kotlinx.cli.ArgParser
import model.Command

fun buildCommand(args: Array<String>): Command {
    return args.firstOrNull()?.let { commandName ->
        val argsParser = ArgParser("codecrafters-git")

        val optionsArgs = (args.toList() - commandName).toTypedArray()

        when (commandName) {
            "init" -> buildInitCommand(optionsArgs, commandName)
            "cat-file" -> buildCatFileCommand(optionsArgs, argsParser, commandName)
            "hash-object" -> buildHashObjectCommand(optionsArgs, argsParser, commandName)
            "ls-tree" -> buildLsTreeCommand(optionsArgs, argsParser, commandName)
            "write-tree" -> buildWriteTreeCommand(commandName)
            "commit-tree" -> buildCommitTreeCommand(optionsArgs, argsParser, commandName)
            "clone" -> buildCloneCommand(optionsArgs, argsParser, commandName)
            else -> throw RuntimeException("Command $commandName is not supported.")
        }
    } ?: throw RuntimeException("No command specified")
}