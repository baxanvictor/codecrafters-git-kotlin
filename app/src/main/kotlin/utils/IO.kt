package utils

import java.nio.file.Files
import java.nio.file.Path

fun Path.writeBytes(bytes: ByteArray) {
    Files.write(this, bytes)
}

fun Path.writeString(string: String) {
    Files.writeString(this, string)
}

fun Path.readAllBytes(): ByteArray {
    return Files.readAllBytes(this)
}