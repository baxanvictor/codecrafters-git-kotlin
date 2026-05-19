package model.exception

class CommandNotSupportedException(
    cmdName: String
) : RuntimeException("Command $cmdName is not supported.")