package utils

fun nullByte(): Byte = Constants.NULL_BYTE.convertToByte()

fun emptySpaceByte(): Byte = Constants.EMPTY_SPACE.convertToByte()

private fun Char.convertToByte(): Byte = code.toByte()