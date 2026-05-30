package processcommand

import model.Command
import model.Sha1Hex
import utils.extractZlibDecompressedContent
import utils.toFullPath
import utils.zlibDecompress
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.io.path.absolute

fun processCatFileCommand(command: Command.CatFile) {
    val path = command.options.sha.toFullPath()
    val rawBytes = Files.readAllBytes(path)
    val decompressedContent = rawBytes
        .zlibDecompress()
        .bytes
        .toString(StandardCharsets.UTF_8)
    val content = decompressedContent.extractZlibDecompressedContent()
    println(content)
}
