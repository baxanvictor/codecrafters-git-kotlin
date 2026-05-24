package processcommand

import model.*
import utils.buildGitObjectContent
import utils.toFormattedCommitTimestamp
import utils.toSha1Hex
import utils.writeGitObject

fun processCommitTreeCommand(command: Command.CommitTree) {
    command.run {
        val commit = GitCommit(
            sha = treeSha,
            parentSha = options.parentSha,
            message = options.message
        )

        val commitObject = commit.toCommitObject()

        val commitSha = writeGitObject(commitObject)

        print(commitSha.toSha1Hex().value)
    }
}

private fun GitCommit.toCommitObject(): ByteArray {
    val commitContent = buildString {
        append(GitObjectType.TREE.type)
        append(' ')
        append(sha.value)
        append('\n')
        parentSha?.let { parentSha ->
            append("parent")
            append(' ')
            append(parentSha.value)
        }
        append('\n')
        append(author.toCommitContentEntry())
        append('\n')
        append(committer.toCommitContentEntry())
        message?.let {
            append("\n\n")
            append(message)
        }
        append('\n')
    }

    return buildGitObjectContent(
        objectType = GitObjectType.COMMIT,
        contentLength = commitContent.length,
        content = commitContent.toByteArray()
    )
}

private fun GitCommitUser.toCommitContentEntry(): String {
    return listOf(
        type.toCommitContentEntryUserType(),
        user.name,
        user.email,
        commitTimestamp.toFormattedCommitTimestamp()
    ).joinToString(" ")
}

private fun GitCommitUserType.toCommitContentEntryUserType(): String {
    return when (this) {
        GitCommitUserType.AUTHOR -> "author"
        GitCommitUserType.COMMITTER -> "committer"
    }
}



