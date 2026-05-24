package utils

import model.Sha1Bytes
import model.Sha1Hex
import java.nio.file.Files
import kotlin.io.path.notExists

fun writeGitObject(gitObject: ByteArray): Sha1Bytes {
    val sha = gitObject.sha1()
    val compressed = gitObject.zlibCompress()

    val writePath = Sha1Hex(sha.toHexString()).toFullPath()
    if (writePath.notExists()) {
        Files.createDirectories(writePath.parent)
    }

    Files.write(writePath, compressed)

    return Sha1Bytes(sha)
}