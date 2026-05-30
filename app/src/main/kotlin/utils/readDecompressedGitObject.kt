package utils

fun readDecompressedGitObject(
    sha: String,
    rootDir: String = Constants.CURRENT_DIR
): ZlibDecompressResult {
    val path = gitObjectsPath(
        sha = sha,
        rootDir = rootDir
    )
    val rawBytes = path.readAllBytes()
    return rawBytes.zlibDecompress()
}