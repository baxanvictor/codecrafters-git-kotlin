package utils

import model.Sha1Hex
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

fun ByteArray.split(delimiter: Byte): List<ByteArray> {
    return buildList {
        val byteArray = this@split
        var start = 0

        for (i in byteArray.indices) {
            if (byteArray[i] == delimiter) {
                add(copyOfRange(start, i))
                start = i + 1
            }
        }

        add(copyOfRange(start, byteArray.size))
    }
}

fun ByteArray.byteArrayBefore(delimiter: Byte): ByteArray {
    for (i in indices) {
        if (this[i] == delimiter) {
            return copyOfRange(0, i)
        }
    }

    return this
}

fun ByteArray.byteArrayAfter(delimiter: Byte): ByteArray {
    for (i in indices) {
        if (this[i] == delimiter && i < size - 1) {
            return (copyOfRange(i + 1, size))
        }
    }

    return this
}

fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}

fun ByteArray.sha1(): ByteArray {
    return MessageDigest
        .getInstance("SHA-1")
        .digest(this)
}

fun ByteArray.sha1Hex(): String {
    val sha = Sha1Hex(
        value = sha1().toHexString()
    )
    if (!sha.isValid()) {
        throw RuntimeException("Invalid sha1: ${sha.value}")
    }

    return sha.value
}

fun ByteArray.toBigEndianInt(offset: Int): Int? {
    if (size < offset + 4) {
        return null
    }

    val byte0 = (get(offset).toInt() and 0xff) shl 24
    val byte1 = (get(offset + 1).toInt() and 0xff) shl 16
    val byte2 = (get(offset + 2).toInt() and 0xff) shl 8
    val byte3 = get(offset + 3).toInt() and 0xff

    return byte0 or byte1 or byte2 or byte3
}

fun ByteArray.applyDelta(delta: ByteArray): ByteArray {
    var offset = 0

    fun readVarInt(): Int {
        var result = 0
        var shift = 0

        while (true) {
            if (offset >= delta.size) {
                throw RuntimeException("Unexpected end of delta while reading variant")
            }

            val b = delta[offset++].toInt() and 0xff
            result = result or ((b and 0x7f) shl shift)

            if ((b and 0x80) == 0) break

            shift += 7
        }

        return result
    }

    val baseSize = readVarInt()
    if (baseSize != size) {
        throw RuntimeException("Delta base size mismatch. Expected $baseSize, got $size")
    }

    val resultSize = readVarInt()
    val output = ByteArrayOutputStream(resultSize)

    while (offset < delta.size) {
        val instruction = delta[offset++].toInt() and 0xff

        if ((instruction and 0x80) != 0) {
            var copyOffset = 0
            var copySize = 0

            if ((instruction and 0x01) != 0) copyOffset = copyOffset or ((delta[offset++].toInt() and 0xff) shl 0)
            if ((instruction and 0x02) != 0) copyOffset = copyOffset or ((delta[offset++].toInt() and 0xff) shl 8)
            if ((instruction and 0x04) != 0) copyOffset = copyOffset or ((delta[offset++].toInt() and 0xff) shl 16)
            if ((instruction and 0x08) != 0) copyOffset = copyOffset or ((delta[offset++].toInt() and 0xff) shl 24)

            if ((instruction and 0x10) != 0) copySize = copySize or ((delta[offset++].toInt() and 0xff) shl 0)
            if ((instruction and 0x20) != 0) copySize = copySize or ((delta[offset++].toInt() and 0xff) shl 8)
            if ((instruction and 0x40) != 0) copySize = copySize or ((delta[offset++].toInt() and 0xff) shl 16)

            if (copySize == 0) {
                copySize = 0x10000
            }

            if (copyOffset + copySize > size) {
                throw RuntimeException("Delta copy exceeds base object size")
            }

            output.write(this, copyOffset, copySize)
        } else {
            if (instruction == 0) {
                throw RuntimeException("Invalid delta instruction: 0")
            }

            if (offset + instruction > delta.size) {
                throw RuntimeException("Delta insert exceeds delta size")
            }

            output.write(delta, offset, instruction)
            offset += instruction
        }
    }

    val result = output.toByteArray()
    if (result.size != resultSize) {
        throw RuntimeException("Delta result size mismatch. Expected $resultSize, got $size")
    }

    return result
}

fun ByteArray.indexOfAfterIndex(byte: Byte, index: Int = 0): Int {
    val clampedIndex = index.coerceIn(
        minimumValue = 0,
        maximumValue = size - 1
    )

    for (i in clampedIndex..< size) {
        if (this[i] == byte) {
            return i
        }
    }

    return -1
}