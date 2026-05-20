package utils

fun String.extractZlibDecompressedContent(): String {
    return substringAfter(Constants.NULL_BYTE)
}