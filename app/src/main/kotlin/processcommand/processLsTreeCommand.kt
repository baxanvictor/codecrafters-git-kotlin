package processcommand

import model.*
import utils.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files

fun processLsTreeCommand(command: Command.LsTree) {
    command.run {
        val treePath = treeSha.toFullPath()
        val treeBytes = Files.readAllBytes(treePath)
        val decompressedTreeContents = treeBytes.zlibDecompress()
        val treeEntries = parseTreeContents(contentBytes = decompressedTreeContents)

        if (options.nameOnly) {
            treeEntries.forEach { entry ->
                println(entry.fsEntryName)
            }
        }
    }
}

private fun parseTreeContents(contentBytes: ByteArray): List<GitTreeEntry> {
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
        var start = 0

        var mode: GitTreeEntryMode? = null
        var fsEntryName: String? = null
        var shaBytes = 0
        var sha: Sha1Bytes? = null

        for (i in treeContents.indices) {
            if (treeContents[i] == emptySpaceAsByte) {
                val modeValue = treeContents.copyOfRange(start, i)
                mode = runCatching {
                    GitTreeEntryMode.entries.firstOrNull { entryMode ->
                        entryMode.mode == modeValue.toString(StandardCharsets.UTF_8)
                    }
                }.getOrElse {
                    throw RuntimeException("Invalid git tree entry mode: $modeValue")
                }

                start = i + 1
            }

            if (mode == null) {
                continue
            }

            if (treeContents[i] == nullByteCodeAsByte) {
                fsEntryName = treeContents.copyOfRange(start, i).toString(StandardCharsets.UTF_8)

                start = i + 1

                continue
            }

            if (fsEntryName == null) {
                continue
            }

            shaBytes++

            if (shaBytes == Constants.TREE_INNER_SHA_LENGTH) {
                sha = Sha1Bytes(
                    value = treeContents.copyOfRange(start, i + 1)
                )

                start = i + 1
            }

            if (sha == null) {
                continue
            }

            add(
                GitTreeEntry(
                    mode = mode,
                    fsEntryName = fsEntryName,
                    sha = sha
                )
            )

            mode = null
            fsEntryName = null
            shaBytes = 0
            sha = null
        }
    }
}