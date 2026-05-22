package utils

import model.ByteArrayOutputStreamInput
import java.io.ByteArrayOutputStream

fun buildByteArrayFromInputs(inputs: List<ByteArrayOutputStreamInput>) : ByteArray {
    val capacity = inputs.sumOf { input ->
        when (input) {
            is ByteArrayOutputStreamInput.IntInput -> 1
            is ByteArrayOutputStreamInput.ByteArrayInput -> input.input.size
        }
    }

    return ByteArrayOutputStream(capacity).apply {
        inputs.forEach { input ->
            when (input) {
                is ByteArrayOutputStreamInput.IntInput -> write(input.input)
                is ByteArrayOutputStreamInput.ByteArrayInput -> writeBytes(input.input)
            }
        }
    }.toByteArray()
}