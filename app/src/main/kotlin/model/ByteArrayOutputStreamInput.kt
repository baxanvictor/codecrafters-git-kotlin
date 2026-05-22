package model

sealed interface ByteArrayOutputStreamInput {
    @JvmInline
    value class IntInput(val input: Int) : ByteArrayOutputStreamInput

    @JvmInline
    value class ByteArrayInput(val input: ByteArray) : ByteArrayOutputStreamInput
}