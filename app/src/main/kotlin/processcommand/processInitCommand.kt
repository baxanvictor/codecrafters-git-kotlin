package processcommand

import model.Command
import java.io.File

fun processInitCommand(command: Command.Init) {
    val gitDir = File(".git")
    gitDir.mkdir()
    File(gitDir, "objects").mkdir()
    File(gitDir, "refs").mkdir()
    File(gitDir, "HEAD").writeText("ref: refs/heads/master\n")

    println("Initialized git directory")
}