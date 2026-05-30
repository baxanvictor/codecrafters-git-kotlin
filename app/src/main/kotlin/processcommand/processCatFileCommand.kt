package processcommand

import model.Command
import utils.Constants
import utils.readDecompressedGitObject

fun processCatFileCommand(command: Command.CatFile) {
    val content = readDecompressedGitObject(sha = command.options.sha.value)
        .bytes
        .decodeToString()
        .substringAfter(Constants.NULL_BYTE)

    print(content)
}
