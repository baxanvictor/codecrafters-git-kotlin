package model

data class GitTree(
    val entries: List<GitTreeEntry>
)

data class GitTreeEntry(
    val mode: GitTreeEntryMode,
    val fsEntryName: String,
    val sha: Sha1
)

enum class GitTreeEntryMode(val mode: String) {
    REGULAR_FILE("100644"),
    EXECUTABLE_FILE("100755"),
    SYMLINK("120000"),
    DIRECTORY("40000")
}
