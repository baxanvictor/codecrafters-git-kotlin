package utils

import java.util.zip.Deflater

fun ByteArray.zlibCompress(): ByteArray {
    val output = ByteArray(size * 4)
    val compressor = Deflater().apply {
        setInput(this@zlibCompress)
        finish()
    }
    val compressedDataLength = compressor.deflate(output)

    return output.copyOfRange(0, compressedDataLength)
}