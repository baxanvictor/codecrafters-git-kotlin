package utils

import model.Sha1Bytes
import model.Sha1Hex

fun Sha1Bytes.toSha1Hex(): Sha1Hex {
    return Sha1Hex.new(
        sha = value.toHexString()
    )
}