package processcommand

import model.Command

fun Command.process() {
    when (this) {
        is Command.Init -> processInit()
        is Command.CatFile -> processCatFile()
        is Command.HashObject -> processHashObject()
        is Command.LsTree -> processLsTreeCommand()
        is Command.WriteTree -> processWriteTreeCommand()
    }
}
