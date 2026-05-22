package model

data class GitTreeEntry(
    val mode: GitTreeEntryMode,
    val fsEntryName: String,
    val sha: Sha1Bytes
)

enum class GitTreeEntryMode(val mode: String) {
    REGULAR_FILE("100644"),
    EXECUTABLE_FILE("100755"),
    SYM_LINK("120000"),
    DIRECTORY("40000")
}
