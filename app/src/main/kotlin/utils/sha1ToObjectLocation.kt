package utils

import model.Sha1

fun Sha1.toObjectLocation(): String {
    return buildString {
        append(toObjectDirectory())
        append('/')
        append(value.substring(2))
    }
}

fun Sha1.toObjectDirectory(): String = value.substring(0, 2)