package utils

import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

fun ByteArray.zlibDecompress(offset: Int = 0): ZlibDecompressResult {
    return ByteArrayOutputStream().use { outputStream ->
        val inflater = Inflater()
        val input = if (offset == 0) {
            this
        } else {
            this.copyOfRange(offset, size)
        }

        try {
            inflater.setInput(input)

            val buffer = ByteArray(1024)

            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && inflater.needsInput()) {
                    throw RuntimeException("Truncated stream during zlib decompression")
                }

                outputStream.write(buffer, 0, count)
            }

            ZlibDecompressResult(
                bytes = outputStream.toByteArray(),
                compressedBytesRead = inflater.bytesRead
            )
        } finally {
            inflater.end()
        }
    }
}

class ZlibDecompressResult(
    val bytes: ByteArray,
    val compressedBytesRead: Long
)