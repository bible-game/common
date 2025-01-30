package game.bible.common.util.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Enables logging in composed classes
 *
 * @author J. R. Smith
 * @since 7th December 2024
 */
@Deprecated("The is better practise in Kotlin, see: https://stackoverflow.com/questions/60419699/implement-slf4j-annotation-from-lombok-in-kotlin")
abstract class Log {
    val log: Logger = LoggerFactory.getLogger(this.javaClass)
}