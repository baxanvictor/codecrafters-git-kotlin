package utils

import model.GitObject
import model.GitObjectType

fun readGitObjectFromShaHex(shaHex: String, rootDir: String): GitObject {
    val gitObjectBytes = readDecompressedGitObject(
        sha = shaHex,
        rootDir = rootDir
    ).bytes

    val nullIndex = gitObjectBytes.indexOf(nullByte())
    if (nullIndex == -1) {
        throw RuntimeException("Invalid git object header for $shaHex")
    }

    val header = gitObjectBytes.copyOfRange(0, nullIndex).decodeToString()
    val content = gitObjectBytes.copyOfRange(nullIndex + 1, gitObjectBytes.size)

    val headerParts = header.split(Constants.EMPTY_SPACE)
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