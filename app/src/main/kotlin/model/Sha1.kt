package model

import utils.isValid

@JvmInline
value class Sha1Hex private constructor(val value: String) {
    companion object {
        fun new(sha: String): Sha1Hex {
            val sha1Hex = Sha1Hex(sha)
            if (!sha1Hex.isValid()) {
                throw RuntimeException("Invalid sha1: $sha")
            }

            return sha1Hex
        }
    }
}

@JvmInline
value class Sha1Bytes(val value: ByteArray)
