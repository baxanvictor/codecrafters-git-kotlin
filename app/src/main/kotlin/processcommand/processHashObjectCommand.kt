package processcommand

import model.Command
import model.GitObjectType
import model.Sha1Hex
import model.exception.InvalidSha1Exception
import utils.*
import java.nio.file.Files
import java.nio.file.Path

fun processHashObjectCommand(command: Command.HashObject) {
    command.run {
        val filePath = Path.of(filename)
        val fileContents = Files.readAllBytes(filePath)

        val blobObjectContent = buildGitObjectContent(
            objectType = GitObjectType.BLOB,
            contentLength = fileContents.size,
            content = fileContents
        )

        val sha1 = Sha1Hex(
            value = blobObjectContent
                .sha1()
                .toHexString()
        )
        if (!sha1.isValid()) {
            throw InvalidSha1Exception(sha1.value)
        }

        println(sha1.value)

        if (options.isWriteEnabled) {
            writeGitObject(blobObjectContent)
        }
    }
}