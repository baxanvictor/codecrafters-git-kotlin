package utils

import java.nio.file.Path

fun gitObjectsPath(sha: String, rootDir: String = "."): Path {
    return gitPath(rootDir)
        .resolve("objects")
        .resolve(sha.take(2))
        .resolve(sha.drop(2))
}

fun gitPath(rootDir: String = "."): Path {
    return rootDirPath(rootDir)
        .resolve(".git")
}

fun rootDirPath(rootDir: String = "."): Path {
    return Path.of(rootDir)
}