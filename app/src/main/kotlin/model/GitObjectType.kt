package model

enum class GitObjectType(val type: String) {
    BLOB("blob"),
    TREE("tree"),
    COMMIT("commit"),
    TAG("tag"),
    OFS_DELTA("ofs-delta"),
    REF_DELTA("ref-delta");

    companion object {
        fun fromPackedObjectNumericId(id: Int): GitObjectType? {
            return when (id) {
                1 -> COMMIT
                2 -> TREE
                3 -> BLOB
                4 -> TAG
                6 -> OFS_DELTA
                7 -> REF_DELTA
                else -> null
            }
        }
    }
}
