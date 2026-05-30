package model

class GitObject(
    val type: GitObjectType,
    val content: ByteArray
)