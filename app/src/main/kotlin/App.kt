import buildcommand.buildCommand
import processcommand.process
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!")

    try {
        val command = buildCommand(args)
        command.process()
    } catch (e: Exception) {
        e.message?.let { println(it) }
        exitProcess(1)
    }
}
