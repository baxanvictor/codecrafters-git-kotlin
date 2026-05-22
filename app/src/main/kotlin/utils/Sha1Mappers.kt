package utils

import model.Sha1Hex
import java.nio.file.Path

fun Sha1Hex.toFullPath(): Path {
    return Path.of("${Constants.GIT_ROOT_DIR}/${toObjectLocation()}")
}

fun Sha1Hex.toDirPath(): Path {
    return Path.of("${Constants.GIT_ROOT_DIR}/${toObjectDirectory()}")
}

private fun Sha1Hex.toObjectLocation(): String {
    return buildString {
        append(toObjectDirectory())
        append('/')
        append(value.substring(2))
    }
}

private fun Sha1Hex.toObjectDirectory(): String = value.substring(0, 2)