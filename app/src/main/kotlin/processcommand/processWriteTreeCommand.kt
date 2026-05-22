package processcommand

import model.*
import utils.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

fun Command.WriteTree.processWriteTreeCommand() {
    val currentPath = Path.of(".")

    val result = createTree(currentPath)

    println(result.sha.value.toHexString())
}

private fun createTree(currentPath: Path): CreateTreeResult {
    if (currentPath.isDirectory()) {
        Files.list(currentPath).use { paths ->
            val createTreeResult = paths
                .filter { it.name != ".git" }
                .map { createTree(it) }
                .toList()

            val treeSha = writeTreeEntry( createTreeResult)

            return CreateTreeResult(
                mode = GitTreeEntryMode.DIRECTORY,
                sha = treeSha,
                path = currentPath
            )
        }
    } else {
        val mode = when {
            currentPath.isRegularFile() -> GitTreeEntryMode.REGULAR_FILE
            currentPath.isExecutable() -> GitTreeEntryMode.EXECUTABLE_FILE
            currentPath.isSymbolicLink() -> GitTreeEntryMode.SYM_LINK
            else -> throw RuntimeException("Unknown mode for path: ${currentPath.name}")
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
    innerCreateTreeResults: List<CreateTreeResult>
): Sha1Bytes {
    val treeInnerObjects = innerCreateTreeResults
        .sortedBy { it.path.name }
        .map { result ->
            buildByteArrayFromInputs(
                inputs = listOf(
                    ByteArrayOutputStreamInput.ByteArrayInput(result.mode.mode.toByteArray()),
                    ByteArrayOutputStreamInput.IntInput(' '.code),
                    ByteArrayOutputStreamInput.ByteArrayInput(result.path.name.toByteArray()),
                    ByteArrayOutputStreamInput.IntInput(Constants.NULL_BYTE.code),
                    ByteArrayOutputStreamInput.ByteArrayInput(result.sha.value)
                )
            )
        }

    return writeGitObject(
        objectType = GitObjectType.TREE,
        innerContent = treeInnerObjects
    )
}

private fun writeBlobEntry(
    path: Path
): Sha1Bytes {
    val bytes = Files.readAllBytes(path)

    return writeGitObject(
        objectType = GitObjectType.BLOB,
        innerContent = listOf(bytes)
    )
}

private fun writeGitObject(
    objectType: GitObjectType,
    innerContent: List<ByteArray>
): Sha1Bytes {
    val gitObject = buildByteArrayFromInputs(
        inputs = buildList {
            add(ByteArrayOutputStreamInput.ByteArrayInput(objectType.type.toByteArray()))
            add(ByteArrayOutputStreamInput.IntInput(' '.code))
            add(
                ByteArrayOutputStreamInput.ByteArrayInput(innerContent.sumOf { it.size }.toString().toByteArray())
            )
            add(ByteArrayOutputStreamInput.IntInput(Constants.NULL_BYTE.code))
            innerContent.forEach { add(ByteArrayOutputStreamInput.ByteArrayInput(it)) }
        }
    )

    val sha = gitObject.sha1()
    val compressed = gitObject.zlibCompress()

    val writePath = Sha1Hex(sha.toHexString()).toFullPath()
    if (writePath.notExists()) {
        Files.createDirectories(writePath.parent)
    }

    Files.write(writePath, compressed)

    return Sha1Bytes(sha)
}

private data class CreateTreeResult(
    val mode: GitTreeEntryMode,
    val sha: Sha1Bytes,
    val path: Path
)
