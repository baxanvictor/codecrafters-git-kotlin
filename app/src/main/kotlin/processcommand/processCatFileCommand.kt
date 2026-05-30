package processcommand

import model.Command
import utils.Constants
import utils.gitObjectsPath
import utils.zlibDecompress
import java.nio.file.Files

fun processCatFileCommand(command: Command.CatFile) {
    val path = gitObjectsPath(
        sha = command.options.sha.value
    )
    val rawBytes = Files.readAllBytes(path)
    val content = rawBytes
        .zlibDecompress()
        .bytes
        .decodeToString()
        .substringAfter(Constants.NULL_BYTE)
    print(content)
}
