package model

data class GitTreeEntry(
    val mode: GitTreeEntryMode,
    val fsEntryName: String,
    val sha: Sha1Bytes
) {
    val isDirectory: Boolean
        get() = mode == GitTreeEntryMode.DIRECTORY

    override fun toString(): String {
        return buildString {
            append("${this@GitTreeEntry::class.simpleName}:\n")
            append("mode = ${mode.mode}\n")
            append("name = $fsEntryName\n")
            append("sha = ${sha.value.toHexString()}\n")
        }
    }
}

enum class GitTreeEntryMode(val mode: String) {
    REGULAR_FILE("100644"),
    EXECUTABLE_FILE("100755"),
    SYM_LINK("120000"),
    DIRECTORY("40000")
}
