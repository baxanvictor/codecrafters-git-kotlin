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
            val sha: Sha1
        )
    }

    data class HashObject(
        override val commandName: String,
        val options: Options
    ) : Command {
        data class Options(
            val isWriteEnabled: Boolean,
            val filename: String
        )
    }

    data class Tree(
        override val commandName: String,
        val options: Options
    ) : Command {
        data class Options(
            val nameOnly: Boolean,
            val treeSha: Sha1
        )
    }
}