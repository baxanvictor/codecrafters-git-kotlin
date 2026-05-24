package model

import java.time.ZonedDateTime

data class GitCommit(
    val sha: Sha1Hex,
    val parentSha: Sha1Hex?,
    val author: GitCommitUser = GitCommitUser.defaultAuthor(),
    val committer: GitCommitUser = GitCommitUser.defaultCommitter(),
    val message: String?
)