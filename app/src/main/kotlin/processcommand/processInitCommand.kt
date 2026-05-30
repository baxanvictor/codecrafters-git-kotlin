package processcommand

import model.Command
import utils.Constants
import utils.gitPath
import kotlin.io.path.createDirectories
import kotlin.io.path.writeLines

fun processInitCommand(command: Command.Init) {
    initGitRepo()

    println("Initialized git directory")
}

fun initGitRepo(rootDir: String = Constants.CURRENT_DIR) {
    val gitDirPath = gitPath(rootDir = rootDir)
    gitDirPath.createDirectories()

    listOf(
        "objects",
        "refs",
        "refs/heads"
    ).forEach { dir ->
        gitDirPath
            .resolve(dir)
            .createDirectories()
    }

    val headFilePath = gitDirPath.resolve("HEAD")
    headFilePath.writeLines(
        lines = listOf("ref: refs/heads/master")
    )
}