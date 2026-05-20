package utils

import java.nio.charset.StandardCharsets
import java.util.zip.Deflater

fun String.zlibCompress(): ByteArray {
    val input = toByteArray(StandardCharsets.UTF_8)

    val output = ByteArray(input.size * 4)
    val compressor = Deflater().apply {
        setInput(input)
        finish()
    }
    val compressedDataLength = compressor.deflate(output)

    return output.copyOfRange(0, compressedDataLength)
}