package processcommand

import model.*
import utils.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

fun processWriteTreeCommand(command: Command.WriteTree) {
    val currentPath = Path.of(".")

    val result = createTree(currentPath)

    println(result.sha.value.toHexString())
}

fun createTree(
    currentPath: Path,
    writePath: Path? = null
): CreateTreeResult {
    if (currentPath.isDirectory()) {
        Files.list(currentPath).use { paths ->
            val createTreeResult = paths
                .filter { it.name != ".git" }
                .map { createTree(it) }
                .toList()

            val treeSha = writeTreeEntry( createTreeResult, writePath)

            return CreateTreeResult(
                mode = GitTreeEntryMode.DIRECTORY,
                sha = treeSha,
                path = currentPath
            )
        }
    } else {
        if (currentPath.notExists()) {
            throw RuntimeException("Path does not exist: ${currentPath.absolute()}")
        }

        val mode = when {
            currentPath.isRegularFile() -> GitTreeEntryMode.REGULAR_FILE
            currentPath.isExecutable() -> GitTreeEntryMode.EXECUTABLE_FILE
            currentPath.isSymbolicLink() -> GitTreeEntryMode.SYM_LINK
            else -> throw RuntimeException("Unknown mode for path: ${currentPath.absolute()}")
        }

        val sha = writeBlobEntry(currentPath, writePath)

        return CreateTreeResult(
            mode = mode,
            sha = sha,
            path = currentPath
        )
    }
}

private fun writeTreeEntry(
    innerCreateTreeResults: List<CreateTreeResult>,
    writePath: Path?
): Sha1Bytes {
    val treeInnerObjects = innerCreateTreeResults
        .sortedBy { it.path.name }
        .map { result ->
            buildByteArrayFromInputs(
                inputs = listOf(
                    ByteArrayOutputStreamInput.ByteArrayInput(result.mode.mode.encodeToByteArray()),
                    ByteArrayOutputStreamInput.IntInput(' '.code),
                    ByteArrayOutputStreamInput.ByteArrayInput(result.path.name.encodeToByteArray()),
                    ByteArrayOutputStreamInput.IntInput(Constants.NULL_BYTE.code),
                    ByteArrayOutputStreamInput.ByteArrayInput(result.sha.value)
                )
            )
        }

    return writeGitObject(
        objectType = GitObjectType.TREE,
        innerContent = treeInnerObjects,
        writePath = writePath
    )
}

private fun writeBlobEntry(
    path: Path,
    writePath: Path?
): Sha1Bytes {
    val bytes = Files.readAllBytes(path)

    return writeGitObject(
        objectType = GitObjectType.BLOB,
        innerContent = listOf(bytes),
        writePath = writePath
    )
}

private fun writeGitObject(
    objectType: GitObjectType,
    innerContent: List<ByteArray>,
    writePath: Path?
): Sha1Bytes {
    val gitObjectContent = buildGitObjectContent(
        objectType = objectType,
        contentLength = innerContent.sumOf { it.size },
        content = buildByteArrayFromInputs(
            inputs = innerContent.map { ByteArrayOutputStreamInput.ByteArrayInput(it) }
        )
    )

    return writeGitObject(gitObjectContent, writePath, objectType = objectType)
}

data class CreateTreeResult(
    val mode: GitTreeEntryMode,
    val sha: Sha1Bytes,
    val path: Path
)
