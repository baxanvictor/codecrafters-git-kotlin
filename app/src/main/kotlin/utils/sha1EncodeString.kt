package utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

fun String.sha1Encode(): String {
    val bytes = toByteArray(StandardCharsets.UTF_8)
    val md = MessageDigest.getInstance("SHA-1")
    val digest = md.digest(bytes)
    return digest.fold("") { str, byte ->
        str + "%02x".format(byte)
    }
}