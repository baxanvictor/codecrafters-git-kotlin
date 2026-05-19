package utils

fun String.extractZlibDecompressedContent(): String {
    var sizeIndex = 0

    for (c in this) {
        when {
            c == ' ' -> sizeIndex = indexOf(c) + 1
            sizeIndex == 0 -> continue
            c.isDigit() -> sizeIndex++
            else -> break
        }
    }

    return substring(sizeIndex + 1)
}