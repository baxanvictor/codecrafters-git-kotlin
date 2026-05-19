package model.exception

class InvalidSha1Exception(
    sha1: String
): RuntimeException("Invalid sha1: $sha1")