package processcommand

import model.Command
import model.Sha1
import utils.Constants
import utils.extractZlibDecompressedContent
import utils.toObjectLocation
import utils.zlibDecompress
import java.nio.file.Files
import java.nio.file.Path

fun Command.CatFile.processCatFile() {
    prettyPrintObject(options.sha1)
}

private fun prettyPrintObject(sha1: Sha1) {
    val path = Path.of("${Constants.GIT_ROOT_DIR}/${sha1.toObjectLocation()}")
    val rawBytes = Files.readAllBytes(path)
    val decompressedContent = rawBytes.zlibDecompress()
    val content = decompressedContent.extractZlibDecompressedContent()
    print(content)
}
