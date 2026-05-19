package processcommand

import model.Command
import model.Sha1
import model.exception.InvalidSha1Exception
import utils.extractZlibDecompressedContent
import utils.zlibDecompress
import java.nio.file.Files
import java.nio.file.Path

fun Command.CatFile.processCatFile() {
    val firstOption = options.firstOrNull()
        ?: throw RuntimeException("$commandName must have options")

    if (firstOption is Command.CatFile.Option.PrettyPrint) {
        prettyPrintObject(firstOption.sha1)
    }
}

private fun prettyPrintObject(sha1: Sha1) {
    val path = Path.of(".git/objects/${sha1.toObjectLocation()}")
    val rawBytes = Files.readAllBytes(path)
    val decompressedContent = rawBytes.zlibDecompress()
    val content = decompressedContent.extractZlibDecompressedContent()
    print(content)
}

private fun Sha1.toObjectLocation(): String {
    if (isValid) {
        return buildString {
            append(value.substring(0, 2))
            append('/')
            append(value.substring(2))
        }
    }

    throw InvalidSha1Exception(value)
}
