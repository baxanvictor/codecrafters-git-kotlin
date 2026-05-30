package utils

import model.GitObjectType
import model.Sha1Bytes
import model.Sha1Hex
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.notExists

fun writeGitObject(
    gitObject: ByteArray,
    writePath: Path? = null,
    objectType: GitObjectType? = null
): Sha1Bytes {
    val sha = gitObject.sha1()
    val compressed = gitObject.zlibCompress()

    val writePath = writePath ?: Sha1Hex(sha.toHexString()).toFullPath()
    writePath.createParentDirectories()

    Files.write(writePath, compressed)

//    println("Written object sha: ${sha.toHexString()} to ${writePath}, type: ${objectType?.type}")
//    if (writePath.exists()) {
//        println("Written to path: ${writePath.absolute()}")
//        println()
//    }

    return Sha1Bytes(sha)
}