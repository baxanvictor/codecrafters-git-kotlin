package processcommand

import model.Command
import model.Sha1
import model.exception.InvalidSha1Exception
import utils.Constants
import utils.isValid
import utils.toObjectDirectory
import utils.sha1Encode
import utils.toObjectLocation
import utils.zlibCompress
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

fun Command.HashObject.processHashObject() {
    val filePath = Path.of(options.filename)
    val fileContents = Files.readString(filePath, StandardCharsets.UTF_8)

    val contentsToEncode = "blob ${fileContents.length}${Constants.NULL_BYTE}$fileContents"

    val sha1 = Sha1(
        value = contentsToEncode.sha1Encode()
    )
    if (!sha1.isValid()) {
        throw InvalidSha1Exception(sha1.value)
    }

    println(sha1.value)

    if (options.isWriteEnabled) {
        val compressedBytes = contentsToEncode.zlibCompress()

        val writePathDir = Path.of("${Constants.GIT_ROOT_DIR}/${sha1.toObjectDirectory()}")
        if (writePathDir.notExists()) {
            writePathDir.createDirectories()
        }

        val writePath = Path.of("${Constants.GIT_ROOT_DIR}/${sha1.toObjectLocation()}")
        Files.write(writePath, compressedBytes)
    }
}