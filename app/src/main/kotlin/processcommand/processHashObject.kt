package processcommand

import model.Command
import model.Sha1
import model.exception.InvalidSha1Exception
import utils.Constants
import utils.isValid40Byte
import utils.sha1Encode
import utils.toDirPath
import utils.toFullPath
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
    if (!sha1.isValid40Byte()) {
        throw InvalidSha1Exception(sha1.value)
    }

    println(sha1.value)

    if (options.isWriteEnabled) {
        val compressedBytes = contentsToEncode.zlibCompress()

        val writePathDir = sha1.toDirPath()
        if (writePathDir.notExists()) {
            writePathDir.createDirectories()
        }

        val writePath = sha1.toFullPath()
        Files.write(writePath, compressedBytes)
    }
}