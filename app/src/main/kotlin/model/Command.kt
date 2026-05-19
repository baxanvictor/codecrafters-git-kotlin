package model

sealed interface Command {
    val commandName: String

    data class Init(
        override val commandName: String
    ) : Command

    data class CatFile(
        override val commandName: String,
        val options: List<Option>
    ) : Command {
        sealed interface Option {
            data class PrettyPrint(
                val sha1: Sha1
            ) : Option
        }
    }
}