package model

sealed interface Command {
    val commandName: String

    data class Init(
        override val commandName: String
    ) : Command

    data class CatFile(
        override val commandName: String,
        val options: Options
    ) : Command {
        data class Options(
            val sha: Sha1Hex
        )
    }

    data class HashObject(
        override val commandName: String,
        val filename: String,
        val options: Options
    ) : Command {
        data class Options(
            val isWriteEnabled: Boolean
        )
    }

    data class LsTree(
        override val commandName: String,
        val treeSha: Sha1Hex,
        val options: Options
    ) : Command {
        data class Options(
            val nameOnly: Boolean
        )
    }

    data class WriteTree(
        override val commandName: String
    ) : Command

    data class CommitTree(
        override val commandName: String,
        val treeSha: Sha1Hex,
        val options: Options
    ) : Command {
        data class Options(
            val parentSha: Sha1Hex?,
            val message: String?
        )
    }

    data class Clone(
        override val commandName: String,
        val repoUrl: String,
        val targetDir: String
    ) : Command
}