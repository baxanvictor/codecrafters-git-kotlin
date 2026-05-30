package model

sealed interface GitRef {
    val name: String
    val commitSha: String

    data class Head(
        override val commitSha: String
    ) : GitRef {
        override val name: String = "HEAD"
    }

    data class BranchTip(
        override val name: String,
        override val commitSha: String
    ) : GitRef
}

