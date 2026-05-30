package processcommand

import model.*
import utils.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

fun processWriteTreeCommand(command: Command.WriteTree) {
    val currentPath = Path.of(Constants.CURRENT_DIR)

    val result = createTree(currentPath)

    println(result.sha.value.toHexString())
}

fun createTree(currentPath: Path): CreateTreeResult {
    if (currentPath.isDirectory()) {
        Files.list(currentPath).use { paths ->
            val createTreeResult = paths
                .filter { it.name != ".git" }
                .map { createTree(it) }
                .toList()

            val treeSha = writeTreeEntry(createTreeResult)

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

        val sha = writeBlobEntry(currentPath)

        return CreateTreeResult(
            mode = mode,
            sha = sha,
            path = currentPath
        )
    }
}

private fun writeTreeEntry(
    innerCreateTreeResults: List<CreateTreeResult>,
    rootDir: String = Constants.CURRENT_DIR
): Sha1Bytes {
    val treeInnerObjects = innerCreateTreeResults
        .sortedBy { it.path.name }
        .map { result ->
            buildByteArrayFromInputs(
                inputs = listOf(
                    ByteArrayOutputStreamInput.ByteArrayInput(result.mode.mode.encodeToByteArray()),
                    ByteArrayOutputStreamInput.IntInput(Constants.EMPTY_SPACE.code),
                    ByteArrayOutputStreamInput.ByteArrayInput(result.path.name.encodeToByteArray()),
                    ByteArrayOutputStreamInput.IntInput(Constants.NULL_BYTE.code),
                    ByteArrayOutputStreamInput.ByteArrayInput(result.sha.value)
                )
            )
        }

    return writeGitObject(
        objectType = GitObjectType.TREE,
        innerContent = treeInnerObjects,
        rootDir = rootDir
    )
}

data class CreateTreeResult(
    val mode: GitTreeEntryMode,
    val sha: Sha1Bytes,
    val path: Path
)
