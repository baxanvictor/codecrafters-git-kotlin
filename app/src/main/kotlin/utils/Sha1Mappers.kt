package utils

import model.Sha1Bytes
import model.Sha1Hex
import java.nio.file.Path

fun Sha1Hex.toFullPath(rootDir: String? = null): Path {
    val rootDirPath = rootDir?.let { "$it/${Constants.GIT_OBJECTS_ROOT_DIR}" } ?: Constants.GIT_OBJECTS_ROOT_DIR
    return Path.of(rootDirPath, toObjectLocation())
}

fun Sha1Hex.toDirPath(rootDir: String? = null): Path {
    val rootDirPath = rootDir?.let { "$it/${Constants.GIT_OBJECTS_ROOT_DIR}" } ?: Constants.GIT_OBJECTS_ROOT_DIR
    return Path.of("$rootDirPath/${toObjectDirectory()}")
}

fun Sha1Bytes.toSha1Hex(): Sha1Hex {
    return Sha1Hex(
        value = value.toHexString()
    )
}

fun Sha1Hex.toObjectLocation(): String {
    return buildString {
        append(toObjectDirectory())
        append('/')
        append(value.substring(2))
    }
}

private fun Sha1Hex.toObjectDirectory(): String = value.substring(0, 2)