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
        val fileContents = filePath.readAllBytes()

        val blobObjectContent = buildGitObjectContent(
            objectType = GitObjectType.BLOB,
            contentLength = fileContents.size,
            content = fileContents
        )

        val sha = blobObjectContent.sha1Hex()

        println(sha)

        if (options.isWriteEnabled) {
            writeGitObject(blobObjectContent)
        }
    }
}