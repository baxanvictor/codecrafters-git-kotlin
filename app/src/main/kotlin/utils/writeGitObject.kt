package utils

import model.ByteArrayOutputStreamInput
import model.GitObjectType
import model.Sha1Bytes
import java.nio.file.Path
import kotlin.io.path.createParentDirectories

fun writeGitObject(
    objectType: GitObjectType,
    innerContent: List<ByteArray>,
    rootDir: String = Constants.CURRENT_DIR
): Sha1Bytes {
    val gitObjectContent = buildGitObjectContent(
        objectType = objectType,
        contentLength = innerContent.sumOf { it.size },
        content = buildByteArrayFromInputs(
            inputs = innerContent.map { ByteArrayOutputStreamInput.ByteArrayInput(it) }
        )
    )

    return writeGitObject(
        gitObject = gitObjectContent,
        rootDir = rootDir
    )
}

fun writeGitObject(
    gitObject: ByteArray,
    rootDir: String = Constants.CURRENT_DIR
): Sha1Bytes {
    val sha = gitObject.sha1()
    val compressed = gitObject.zlibCompress()

    val writePath = gitObjectsPath(
        sha = sha.toHexString(),
        rootDir = rootDir
    )
    writePath.createParentDirectories()

    writePath.writeBytes(compressed)

    return Sha1Bytes(sha)
}

fun writeBlobEntry(
    path: Path,
    rootDir: String = Constants.CURRENT_DIR
): Sha1Bytes {
    return writeGitObject(
        objectType = GitObjectType.BLOB,
        innerContent = listOf(path.readAllBytes()),
        rootDir = rootDir
    )
}