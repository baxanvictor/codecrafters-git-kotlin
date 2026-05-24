package model

import utils.Constants
import java.time.ZonedDateTime

data class GitCommitUser(
    val type: GitCommitUserType,
    val user: GitUser,
    val commitTimestamp: ZonedDateTime
) {
    companion object {
        fun defaultAuthor() = GitCommitUser(
            type = GitCommitUserType.AUTHOR,
            user = GitUser(
                name = Constants.DEFAULT_AUTHOR_NAME,
                email = Constants.DEFAULT_AUTHOR_EMAIL
            ),
            commitTimestamp = ZonedDateTime.now()
        )

        fun defaultCommitter() = GitCommitUser(
            type = GitCommitUserType.COMMITTER,
            user = GitUser(
                name = Constants.DEFAULT_COMMITTER_NAME,
                email = Constants.DEFAULT_COMMITTER_EMAIL
            ),
            commitTimestamp = ZonedDateTime.now()
        )
    }
}

enum class GitCommitUserType {
    AUTHOR,
    COMMITTER
}