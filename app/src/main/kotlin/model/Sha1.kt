package model

@JvmInline
value class Sha1(val value: String) {
    val isValid: Boolean
        get() = value.length == 40
}
