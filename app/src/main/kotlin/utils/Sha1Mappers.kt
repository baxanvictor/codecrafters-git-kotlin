package utils

import model.Sha1
import java.nio.file.Path

fun Sha1.toFullPath(): Path {
    return Path.of("${Constants.GIT_ROOT_DIR}/${toObjectLocation()}")
}

fun Sha1.toDirPath(): Path {
    return Path.of("${Constants.GIT_ROOT_DIR}/${toObjectDirectory()}")
}

private fun Sha1.toObjectLocation(): String {
    return buildString {
        append(toObjectDirectory())
        append('/')
        append(value.substring(2))
    }
}

private fun Sha1.toObjectDirectory(): String = value.substring(0, 2)