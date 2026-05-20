package processcommand

import model.Command
import model.Sha1
import utils.extractZlibDecompressedContent
import utils.toFullPath
import utils.zlibDecompress
import java.nio.charset.StandardCharsets
import java.nio.file.Files

fun Command.CatFile.processCatFile() {
    prettyPrintObject(options.sha)
}

private fun prettyPrintObject(sha1: Sha1) {
    val path = sha1.toFullPath()
    val rawBytes = Files.readAllBytes(path)
    val decompressedContent = rawBytes.zlibDecompress().toString(StandardCharsets.UTF_8)
    val content = decompressedContent.extractZlibDecompressedContent()
    print(content)
}
