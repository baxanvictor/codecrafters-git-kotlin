package processcommand

import model.*
import utils.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files

fun processLsTreeCommand(command: Command.LsTree) {
    command.run {
        val treePath = treeSha.toFullPath()
        val treeBytes = Files.readAllBytes(treePath)
        val decompressedTreeContents = treeBytes.zlibDecompress().bytes
        val treeEntries = parseTreeContents(contentBytes = decompressedTreeContents)

        if (options.nameOnly) {
            treeEntries.forEach { entry ->
                println(entry.fsEntryName)
            }
        }
    }
}

fun parseTreeContents(contentBytes: ByteArray): List<GitTreeEntry> {
    val nullByteCodeAsByte = Constants.NULL_BYTE.code.toByte()
    val emptySpaceAsByte = ' '.code.toByte()

    val header = contentBytes.byteArrayBefore(nullByteCodeAsByte)
    val headerPieces = header.split(emptySpaceAsByte)

    if (headerPieces.size != 2) {
        throw RuntimeException("Invalid tree header format: $header")
    }

    val firstHeaderPieceAsString = headerPieces.first().toString(StandardCharsets.UTF_8)
    if (firstHeaderPieceAsString != "tree") {
        throw RuntimeException("Invalid tree header name: $firstHeaderPieceAsString")
    }

    val secondHeaderPieceAsString = headerPieces[1].toString(StandardCharsets.UTF_8)
    val contentsSize = secondHeaderPieceAsString.toIntOrNull()
        ?: throw RuntimeException("Invalid contents size value: $secondHeaderPieceAsString")

    val treeContents = contentBytes.byteArrayAfter(nullByteCodeAsByte)

    if (treeContents.size != contentsSize) {
        throw RuntimeException("Wrong contents size: ${treeContents.size}")
    }

    return buildList {
        var index = 0

        while (index < treeContents.size) {
            val nextEmptySpaceIndex = treeContents.indexOfAfterIndex(
                byte = ' '.code.toByte(),
                index = index
            )

            if (nextEmptySpaceIndex == -1) {
                throw RuntimeException("No more empty spaces, but there are still bytes to read")
            }

            val modeValue = treeContents.copyOfRange(index, nextEmptySpaceIndex)
            val mode = runCatching {
                GitTreeEntryMode.entries.firstOrNull { entryMode ->
                    entryMode.mode == modeValue.decodeToString()
                }
            }.getOrNull()
                ?: throw RuntimeException("Invalid git tree entry mode: $modeValue")

            index = nextEmptySpaceIndex + 1

            val nextNullByteIndex = treeContents.indexOfAfterIndex(
                byte = Constants.NULL_BYTE.code.toByte(),
                index = index
            )

            if (nextNullByteIndex == -1) {
                throw RuntimeException("No more null bytes, but there are still bytes to read")
            }

            val fsFilename = treeContents
                .copyOfRange(index, nextNullByteIndex)
                .decodeToString()

            index = nextNullByteIndex + 1

            if (treeContents.size - index < Constants.TREE_INNER_SHA_LENGTH) {
                throw RuntimeException("Not enough sha bytes")
            }

            val shaEnd = index + Constants.TREE_INNER_SHA_LENGTH

            val sha = Sha1Bytes(
                value = treeContents.copyOfRange(index, shaEnd)
            )

            add(
                GitTreeEntry(
                    mode = mode,
                    fsEntryName = fsFilename,
                    sha = sha
                )
            )

            index = shaEnd
        }
    }
}