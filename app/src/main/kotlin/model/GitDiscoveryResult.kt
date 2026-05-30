package model

data class GitDiscoveryResult(
    val refs: List<GitRef>,
    val capabilities: Set<String>
)

