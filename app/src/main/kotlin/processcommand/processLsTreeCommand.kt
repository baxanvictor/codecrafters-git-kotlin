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
        var mode: GitTreeEntryMode? = null
        var fsEntryName: String? = null

        var i = 0
        var start = 0

        while (i < treeContents.size) {
            if (treeContents[i] == emptySpaceAsByte) {
                val modeValue = treeContents.copyOfRange(start, i)
                mode = runCatching {
                    GitTreeEntryMode.entries.firstOrNull { entryMode ->
                        entryMode.mode == modeValue.decodeToString()
                    }
                }.getOrElse {
                    throw RuntimeException("Invalid git tree entry mode: $modeValue")
                }

                start = ++i
            }

            if (mode == null) {
                i++
                continue
            }

            if (treeContents[i] == nullByteCodeAsByte) {
                fsEntryName = treeContents.copyOfRange(start, i).decodeToString()

                i++
                start = i

                continue
            }

            if (fsEntryName == null) {
                i++
                continue
            }

            val sha = Sha1Bytes(
                value = treeContents.copyOfRange(start, start + Constants.TREE_INNER_SHA_LENGTH)
            )

            i += Constants.TREE_INNER_SHA_LENGTH
            start = i

            add(
                GitTreeEntry(
                    mode = mode,
                    fsEntryName = fsEntryName,
                    sha = sha
                )
            )

            mode = null
            fsEntryName = null
        }
    }
}

fun parseTreeContentsNew(contentBytes: ByteArray): List<GitTreeEntry> {
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

            println("Created mode: $mode")

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

            println("Created filename: $fsFilename")

            index = nextNullByteIndex + 1

            if (treeContents.size - index < Constants.TREE_INNER_SHA_LENGTH) {
                throw RuntimeException("Not enough sha bytes")
            }

            val shaEnd = index + Constants.TREE_INNER_SHA_LENGTH

            val sha = Sha1Bytes(
                value = treeContents.copyOfRange(index, shaEnd)
            )

            println("Created sha: ${sha.toSha1Hex().value}")

            add(
                GitTreeEntry(
                    mode = mode,
                    fsEntryName = fsFilename,
                    sha = sha
                ).also {
                    println(it)
                }
            )

            index = shaEnd
        }
    }
}