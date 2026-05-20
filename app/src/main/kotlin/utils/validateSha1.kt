package utils

import model.Sha1

fun Sha1.isValid40Byte() = value.length == 40