package processcommand

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import model.*
import utils.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeLines

fun processCloneCommand(command: Command.Clone) {
    command.run {
        initRepo(targetDir)

        val ktorClient = initKtorClient(repoUrl)

        val discoverRefsResponse = runBlocking {
            ktorClient.discoverRefs()
        }
        if (!discoverRefsResponse.status.isSuccess()) {
            throw RuntimeException("Failed to discover refs for repo $repoUrl")
        }

        val pktLines = runBlocking {
            discoverRefsResponse.bodyAsBytes().parsePktLines()
        }

        val gitDiscoveryResult = parsePktLines(pktLines)

        val commitSha = gitDiscoveryResult.findCommitSha()

        val negotiateUploadPackResponse = runBlocking {
            ktorClient.negotiateUploadPack(
                commitSha = commitSha,
                capabilities = gitDiscoveryResult.capabilities
            )
        }
        if (!negotiateUploadPackResponse.status.isSuccess()) {
            throw RuntimeException("Failed to negotiate upload pack")
        }

        val negotiateUploadPackPktLines = runBlocking {
            negotiateUploadPackResponse.bodyAsBytes().parsePktLines()
        }

        val packFile = parseNegotiateUploadPackPktLines(negotiateUploadPackPktLines)
        parsePackFileAndWriteGitObjects(packFile, targetDir)

        writeGitRefs(gitDiscoveryResult.refs, targetDir, commitSha)

        checkout(commitSha, targetDir)
    }
}

private fun initRepo(targetDir: String) {
    val gitDirPath = gitPath(rootDir = targetDir)
    gitDirPath.createDirectories()

    listOf(
        "objects",
        "refs",
        "refs/heads"
    ).forEach { dir ->
        gitDirPath
            .resolve(dir)
            .createDirectories()
    }

    val headFilePath = gitDirPath.resolve("HEAD")
    headFilePath.writeLines(
        lines = listOf("ref: refs/heads/master")
    )
}

private fun initKtorClient(repoUrl: String): HttpClient {
    val baseRepoUrl = if (repoUrl.endsWith('/')) repoUrl else "$repoUrl/"

    return HttpClient(CIO) {
        defaultRequest {
            url(baseRepoUrl)
        }
    }
}

private suspend fun HttpClient.discoverRefs(): HttpResponse {
    return get("info/refs?service=git-upload-pack") {
        header("Git-Protocol", "version=1")
    }
}

private suspend fun HttpClient.negotiateUploadPack(
    commitSha: String,
    capabilities: Set<String>
): HttpResponse {
    var requestedCapabilities = buildList {
        val sideBand64k = "side-band-64k"
        if (sideBand64k in capabilities) {
            add(sideBand64k)
        }
    }.joinToString(" ")
    if (requestedCapabilities.isNotBlank()) {
        requestedCapabilities = " $requestedCapabilities"
    }

    val requestBody = encodePktLine("want $commitSha$requestedCapabilities\n") +
            encodeFlushPkt() +
            encodePktLine("done\n")

    return post("git-upload-pack") {
        contentType(
            ContentType(
                contentType = "application",
                contentSubtype = "x-git-upload-pack-request"
            )
        )
        setBody(requestBody)
    }
}

private fun ByteArray.parsePktLines(): List<PktLine> {
    val lines = mutableListOf<PktLine>()
    var index = 0

    while (index < size) {
        if (index + 4 > size) {
            throw RuntimeException("Truncated pkt-line length")
        }

        val length = copyOfRange(index, index + 4)
        val lengthHex = length.decodeToString()
        val packetLength = lengthHex.toIntOrNull(16)
            ?: throw RuntimeException("Invalid length hex: $lengthHex")

        index += 4

        if (packetLength == 0) {
            lines.add(PktLine.Flush)
            continue
        }

        if (packetLength < 4) {
            throw RuntimeException("Invalid pkt-line length: $packetLength")
        }

        val payloadLength = packetLength - 4

        if (index + payloadLength > size) {
            throw RuntimeException("Truncated pkt-line payload")
        }

        val packetPayload = copyOfRange(index, index + payloadLength)
        lines.add(
            PktLine.Data(
                payload = packetPayload
            )
        )

        index += payloadLength
    }

    return lines
}

private fun parsePktLines(lines: List<PktLine>): GitDiscoveryResult {
    val refs = mutableListOf<GitRef>()
    val capabilities = mutableSetOf<String>()

    for (line in lines) {
        when (line) {
            PktLine.Flush -> continue
            is PktLine.Data -> {
                val decodedData = line.payload.decodeToString()
                if (decodedData.contains("service=git-upload-pack")) {
                    continue
                }

                val refAndCapabilities = decodedData.split(Constants.NULL_BYTE)
                when (refAndCapabilities.size) {
                    2 -> {
                        refs.add(extractGitRef(refAndCapabilities.first()))
                        capabilities.addAll(
                            refAndCapabilities
                                .last()
                                .trim()
                                .split(' ')
                                .filter { it.isNotBlank() }
                        )
                    }

                    1 -> refs.add(extractGitRef(decodedData))

                    else -> throw RuntimeException("Wrong line format: $decodedData")
                }
            }
        }
    }

    return GitDiscoveryResult(
        refs = refs,
        capabilities = capabilities
    )
}

private fun parseNegotiateUploadPackPktLines(lines: List<PktLine>): ByteArray {
    val packLines = mutableListOf<ByteArray>()

    for (line in lines) {
        line as? PktLine.Data ?: continue

        if (line.payload.contentEquals("NAK\n".encodeToByteArray())) {
            continue
        }

        if (line.payload.isEmpty()) {
            continue
        }

        val band = line.payload[0].toInt() and 0xff
        val actualPayload = line
            .payload
            .copyOfRange(1, line.payload.size)

        when (band) {
            1 -> {
                packLines.add(actualPayload)
                if (packLines.size == 1) {
                    if (actualPayload.size < 4) {
                        throw RuntimeException("First pack chunk is too small. Size is ${actualPayload.size}")
                    }

                    val prefix = actualPayload.copyOfRange(0, 4)
                    if (prefix.decodeToString() != "PACK") {
                        throw RuntimeException("Invalid pack file start: $prefix")
                    }
                }
            }

            2 -> println(actualPayload.decodeToString())
            3 -> throw RuntimeException("Fatal server error: ${actualPayload.decodeToString()}")
            else -> throw RuntimeException("Unexpected band: $band")
        }
    }

    return buildByteArrayFromInputs(
        inputs = packLines.map { ByteArrayOutputStreamInput.ByteArrayInput(it) }
    )
}

private fun parsePackFileAndWriteGitObjects(packFile: ByteArray, targetDir: String) {
    if (packFile.size < 12) {
        throw RuntimeException("pack file is too short. Its size is ${packFile.size}")
    }

    val packFileStart = packFile.copyOfRange(0, 4).decodeToString()
    if (packFileStart != "PACK") {
        throw RuntimeException("Invalid pack file start: $packFileStart")
    }

    val version = packFile.toBigEndianInt(offset = 4)
        ?: throw RuntimeException("Invalid pack file version")
    if (version !in listOf(2, 3)) {
        throw RuntimeException("Wrong version: $version")
    }

    val objectCount = packFile.toBigEndianInt(offset = 8)
        ?: throw RuntimeException("Invalid pack file object count")

    var offset = 12

    repeat(objectCount) {
        var currentByte = packFile[offset].toInt() and 0xff

        val typeId = (currentByte shr 4) and 0b111
        val type = GitObjectType.fromPackedObjectNumericId(typeId)
            ?: throw RuntimeException("Unsupported packed object type: $typeId")

        if (type == GitObjectType.OFS_DELTA) {
            throw RuntimeException("${type.type} not supported yet")
        }

        var size = currentByte and 0b1111

        var shift = 4

        while ((currentByte and 0x80) != 0) {
            currentByte = packFile[++offset].toInt() and 0xff
            size = size or ((currentByte and 0b01111111) shl shift)
            shift += 7
        }

        val dataStart = offset + 1

        if (type == GitObjectType.REF_DELTA) {
            val baseShaBytes = packFile.copyOfRange(dataStart, dataStart + 20)
            val baseShaHex = baseShaBytes.toHexString()

            val compressedStartOffset = dataStart + 20
            val deltaResult = packFile.zlibDecompress(offset = compressedStartOffset)

            val baseObject = readGitObjectFromShaHex(baseShaHex, rootDir = targetDir)
            val resolvedContent = baseObject.content.applyDelta(deltaResult.bytes)

            writeGitObject(
                objectType = baseObject.type,
                innerContent = listOf(resolvedContent),
                rootDir = targetDir
            )

            offset = compressedStartOffset + deltaResult.compressedBytesRead.toInt()
        } else {
            val decompressResult = packFile.zlibDecompress(offset = dataStart)

            val decompressedSize = decompressResult.bytes.size
            if (decompressResult.bytes.size != size) {
                throw RuntimeException("Wrong number of bytes decompressed. Actual: $decompressedSize, should be: $size")
            }

            writeGitObject(
                objectType = type,
                innerContent = listOf(decompressResult.bytes),
                rootDir = targetDir
            )

            offset = dataStart + decompressResult.compressedBytesRead.toInt()
        }
    }
}

private fun writeGitRefs(refs: List<GitRef>, targetDir: String, commitSha: String) {
    val rootGitPath = gitPath(rootDir = targetDir)

    val path = rootGitPath
        .resolve("refs/heads/master")
    path.createParentDirectories()
    path.writeString("$commitSha\n")

    val headPath = rootGitPath
        .resolve("HEAD")
    headPath.createParentDirectories()

    headPath.writeString("ref: refs/heads/master\n")
}

private fun checkout(commitSha: String, targetDir: String) {
    val commitObjectPath = gitObjectsPath(
        sha = commitSha,
        rootDir = targetDir
    )

    val commitObject = commitObjectPath.readAllBytes()
    val decompressedCommitObject = commitObject.zlibDecompress().bytes

    val nullByteIndex = decompressedCommitObject.indexOf(Constants.NULL_BYTE.code.toByte())
    if (nullByteIndex == -1) {
        throw RuntimeException("Invalid commit object header")
    }

    val treeShaStart = nullByteIndex + 6
    val treeShaBytes = decompressedCommitObject.copyOfRange(treeShaStart, treeShaStart + 40)

    val treeSha = treeShaBytes.decodeToString()
    val rootCheckoutDir = rootDirPath(rootDir = targetDir)

    checkoutTree(treeSha, targetDir, rootCheckoutDir)
}

private fun checkoutTree(treeSha: String, projectDir: String, rootCheckoutDir: Path) {
    val treePath = gitObjectsPath(
        sha = treeSha,
        rootDir = projectDir
    )

    rootCheckoutDir.createDirectories()

    val treeBytes = treePath.readAllBytes()
    val decompressedTreeBytes = treeBytes.zlibDecompress().bytes
    val entries = parseTreeContents(decompressedTreeBytes)

    for (entry in entries) {
        val entryPath = rootCheckoutDir.resolve(entry.fsEntryName)
        val entrySha = entry.sha.toSha1Hex().value

        if (entry.isDirectory) {
            checkoutTree(
                treeSha = entrySha,
                projectDir = projectDir,
                rootCheckoutDir = entryPath
            )
        } else {
            val gitObjectPath = gitObjectsPath(
                sha = entrySha,
                rootDir = projectDir
            )

            val blob = gitObjectPath.readAllBytes()
            val decompressedBlob = blob.zlibDecompress().bytes
            val nullByteIndex = decompressedBlob.indexOf(Constants.NULL_BYTE.code.toByte())

            entryPath.writeBytes(
                bytes = decompressedBlob.copyOfRange(nullByteIndex + 1, decompressedBlob.size)
            )
        }
    }
}

private fun extractGitRef(ref: String): GitRef {
    val refPieces = ref.trim()
        .split(' ')
        .filter { it.isNotBlank() }
    if (refPieces.size != 2) {
        throw RuntimeException("Wrong ref format: ${ref.first()}")
    }

    return GitRef(
        name = refPieces.last(),
        sha = refPieces.first()
    )
}

private fun GitDiscoveryResult.findCommitSha(): String {
    val mainBranch = capabilities
        .firstOrNull { capability ->
            capability.startsWith("symref=HEAD:refs/heads/")
        }?.let { capability ->
            val pieces = capability.split(':')
            if (pieces.size != 2) {
                return@let null
            }

            pieces.last()
        }
        ?: refs
            .firstOrNull { it.name == "refs/heads/master" }
            ?.name
        ?: refs
            .firstOrNull { it.name == "refs/heads/main" }
            ?.name
        ?: refs
            .firstOrNull { it.name == "HEAD" }
            ?.name
        ?: throw RuntimeException("No main or HEAD branch capability or git ref ")

    return refs
        .firstOrNull { it.name == mainBranch }
        ?.sha
        ?: throw RuntimeException("No commit sha found for identifier main branch: $mainBranch")
}

private fun encodePktLine(input: String): ByteArray {
    val inputBytes = input.encodeToByteArray()
    val totalLength = inputBytes.size + 4
    val lengthPrefix = totalLength.toString(16).padStart(4, '0')
    return lengthPrefix.encodeToByteArray() + inputBytes
}

private fun encodeFlushPkt(): ByteArray = "0000".encodeToByteArray()
