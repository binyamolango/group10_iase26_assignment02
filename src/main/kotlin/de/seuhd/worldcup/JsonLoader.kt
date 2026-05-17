package de.seuhd.worldcup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

fun interface UrlFetcher {
    fun fetch(url: String): InputStream
}

/**
 * Loads the bundled tournament data from the classpath. The JSON lives under
 * `src/main/resources`, so this works from any working directory.
 */
object JsonLoader {

    @OptIn(ExperimentalSerializationApi::class)
    fun loadJson(resourcePath: String = "/world_cup_2026_full_data.json"): WorldCupData {
        val stream = JsonLoader::class.java.getResourceAsStream(resourcePath)
            ?: error("Resource not found on classpath: $resourcePath")
        return stream.use { Json.decodeFromStream(it) }
    }

    private val urls = listOf(
        "https://assets.empirical-software.engineering/teaching/26_ss/iase/world_cup_2026_full_data.json",
        "https://assets.empirical-software.engineering/teaching/26_ss/iase/world_cup_2026_full_data_v2.json",
        "http://192.0.2.1/world_cup_2026_full_data.json"
    )

    @OptIn(ExperimentalSerializationApi::class)
    fun loadJsonFromNetwork(
        fetcher: UrlFetcher = { url ->
            val conn = URI(url).toURL().openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 5000
            conn.inputStream
        }
    ): WorldCupData {
        var lastError: Exception? = null
        // Fix: removed shuffled() so URL order is deterministic and the dead IP is always tried last
        for (url in urls) {
            try {
                return fetcher.fetch(url).use { Json.decodeFromStream(it) }
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: error("All URLs failed")
    }
}
