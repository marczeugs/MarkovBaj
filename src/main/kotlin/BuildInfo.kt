import java.util.*

object BuildInfo {
    private val properties = Properties().apply { load(this@BuildInfo::class.java.getResourceAsStream("/buildinfo.properties")) }

    val version = properties.getProperty("version")
}