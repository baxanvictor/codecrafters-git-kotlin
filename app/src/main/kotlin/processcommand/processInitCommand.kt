package processcommand

import model.Command
import java.io.File

fun Command.Init.processInit() {
    val gitDir = File(".git")
    gitDir.mkdir()
    File(gitDir, "objects").mkdir()
    File(gitDir, "refs").mkdir()
    File(gitDir, "HEAD").writeText("ref: refs/heads/master\n")

    println("Initialized git directory")
}