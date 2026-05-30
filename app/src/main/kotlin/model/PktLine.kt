package model

sealed interface PktLine {
    class Data(
        val payload: ByteArray
    ) : PktLine

    data object Flush : PktLine
}