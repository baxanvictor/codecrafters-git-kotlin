package processcommand

import model.ByteArrayOutputStreamInput
import model.Command
import model.GitObjectType
import model.Sha1Hex
import model.exception.InvalidSha1Exception
import utils.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

fun Command.HashObject.processHashObject() {
    val filePath = Path.of(options.filename)
    val fileContents = Files.readAllBytes(filePath)

    val contentsToEncode = buildByteArrayFromInputs(
        inputs = listOf(
            ByteArrayOutputStreamInput.ByteArrayInput(GitObjectType.BLOB.type.toByteArray()),
            ByteArrayOutputStreamInput.IntInput(' '.code),
            ByteArrayOutputStreamInput.ByteArrayInput(fileContents.size.toString().toByteArray()),
            ByteArrayOutputStreamInput.IntInput(Constants.NULL_BYTE.code),
            ByteArrayOutputStreamInput.ByteArrayInput(fileContents)
        )
    )

    val sha1 = Sha1Hex(
        value = contentsToEncode
            .sha1()
            .toHexString()
    )
    if (!sha1.isValid()) {
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