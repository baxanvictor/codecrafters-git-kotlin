package utils

import model.GitObjectType
import model.Sha1Bytes
import model.Sha1Hex
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories

fun writeGitObject(
    gitObject: ByteArray,
    writePath: Path? = null,
    objectType: GitObjectType? = null
): Sha1Bytes {
    val sha = gitObject.sha1()
    val compressed = gitObject.zlibCompress()

    val writePath = writePath ?: gitObjectsPath(sha = sha.toHexString())
    writePath.createParentDirectories()

    Files.write(writePath, compressed)

    return Sha1Bytes(sha)
}