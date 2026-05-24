package processcommand

import model.Command

fun processCommand(command: Command) {
    when (command) {
        is Command.Init -> processInitCommand(command)
        is Command.CatFile -> processCatFileCommand(command)
        is Command.HashObject -> processHashObjectCommand(command)
        is Command.LsTree -> processLsTreeCommand(command)
        is Command.WriteTree -> processWriteTreeCommand(command)
        is Command.CommitTree -> processCommitTreeCommand(command)
    }
}
