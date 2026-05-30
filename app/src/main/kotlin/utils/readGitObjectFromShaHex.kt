package utils

import model.GitObject
import model.GitObjectType
import model.Sha1Hex
import java.nio.file.Files

fun readGitObjectFromShaHex(shaHex: String, rootDir: String): GitObject {
    val objectPath = gitObjectsPath(
        sha = shaHex,
        rootDir = rootDir
    )
    val compressed = Files.readAllBytes(objectPath)
    val raw = compressed.zlibDecompress().bytes

    val nullIndex = raw.indexOf(Constants.NULL_BYTE.code.toByte())
    if (nullIndex == -1) {
        throw RuntimeException("Invalid git object header for $shaHex")
    }

    val header = raw.copyOfRange(0, nullIndex).decodeToString()
    val content = raw.copyOfRange(nullIndex + 1, raw.size)

    val headerParts = header.split(' ')
    if (headerParts.size != 2) {
        throw RuntimeException("Invalid git object header: $header")
    }

    val type = GitObjectType
        .entries
        .firstOrNull { it.type == headerParts[0] }
        ?: throw RuntimeException("Unsupported git object type: ${headerParts[0]}")

    val expectedSize = headerParts[1].toInt()
    if (content.size != expectedSize) {
        throw RuntimeException("Git object size mismatch. Expected $expectedSize, got ${content.size}")
    }

    return GitObject(
        type = type,
        content = content
    )
}