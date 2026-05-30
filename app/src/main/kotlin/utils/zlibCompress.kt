package utils

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater

fun ByteArray.zlibCompress(): ByteArray {
    return ByteArrayOutputStream().use { outputStream ->
        val deflater = Deflater()
        try {
            deflater.setInput(this)
            deflater.finish()

            val buffer = ByteArray(1024)

            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                if (count == 0 && !deflater.finished()) {
                    throw RuntimeException("Truncated stream during zlib compression")
                }

                outputStream.write(buffer, 0, count)
            }

            outputStream.toByteArray()
        } finally {
            deflater.end()
        }
    }
}