package processcommand

import model.Command
import utils.Constants
import utils.toFullPath
import utils.zlibDecompress
import java.nio.file.Files

fun processCatFileCommand(command: Command.CatFile) {
    val path = command.options.sha.toFullPath()
    val rawBytes = Files.readAllBytes(path)
    val content = rawBytes
        .zlibDecompress()
        .bytes
        .decodeToString()
        .substringAfter(Constants.NULL_BYTE)
    print(content)
}
