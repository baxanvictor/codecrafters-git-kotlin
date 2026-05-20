package utils

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