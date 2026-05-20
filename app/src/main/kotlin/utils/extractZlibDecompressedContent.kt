package utils

fun String.extractZlibDecompressedContent(): String {
    return substring(indexOf(Constants.NULL_BYTE) + 1)
}