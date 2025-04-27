package com.example.unlocked.data.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WikidataResult(
    val area: Double? = null, // in km²
    val population: Int? = null,
    val elevationAboveSeaLevel: Double? = null // in meters
)

object WikidataService {
    private const val TAG = "WikidataService"
    private const val WIKIDATA_ENDPOINT = "https://www.wikidata.org/w/api.php"
    private const val SPARQL_ENDPOINT = "https://query.wikidata.org/sparql"

    suspend fun getCityData(cityName: String, countryName: String?): WikidataResult? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching data for city: $cityName, country: $countryName")

            val wikidataId = searchForCityWikidataId(cityName, countryName)
            if (wikidataId == null) {
                Log.w(TAG, "No Wikidata ID found for $cityName")
                return@withContext null
            }

            Log.d(TAG, "Found Wikidata ID: $wikidataId")

            val result = fetchCityData(wikidataId)
            Log.d(TAG, "Wikidata result: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Wikidata: ${e.message}", e)
            null
        }
    }

    private fun searchForCityWikidataId(cityName: String, countryName: String?): String? {
        val searchQuery = if (countryName != null) {
            "$cityName, $countryName"
        } else {
            cityName
        }

        val url = URL("$WIKIDATA_ENDPOINT?action=wbsearchentities" +
                "&search=${URLEncoder.encode(searchQuery, "UTF-8")}" +
                "&language=en" +
                "&limit=5" +
                "&format=json")

        Log.d(TAG, "Search URL: $url")

        return try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "UnlockedApp/1.0 (https://example.com/unlocked; contact@example.com)")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Search response: $response")

                val json = JSONObject(response)
                val searchResults = json.getJSONArray("search")

                // Find the first result that's a city or municipality
                for (i in 0 until searchResults.length()) {
                    val result = searchResults.getJSONObject(i)
                    val description = result.optString("description", "").lowercase()
                    Log.d(TAG, "Result $i: ${result.optString("label")} - $description")

                    if (description.contains("city") ||
                        description.contains("municipality") ||
                        description.contains("town") ||
                        description.contains("village") ||
                        description.contains("capital")) {
                        return result.getString("id")
                    }
                }
                null
            } else {
                Log.e(TAG, "Search failed with code: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching Wikidata: ${e.message}", e)
            null
        }
    }

    private fun fetchCityData(wikidataId: String): WikidataResult? {
        // SPARQL query to get area, population, and elevation
        val query = """
            SELECT ?area ?population ?elevation WHERE {
              OPTIONAL {
                wd:$wikidataId p:P2046 ?areaStatement.
                ?areaStatement ps:P2046 ?area.
              }
              OPTIONAL { wd:$wikidataId wdt:P1082 ?population. }
              OPTIONAL { wd:$wikidataId wdt:P2044 ?elevation. }
            }
            LIMIT 1
        """.trimIndent()

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = URL("$SPARQL_ENDPOINT?query=$encodedQuery&format=json")

        Log.d(TAG, "SPARQL URL: $url")

        return try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "UnlockedApp/1.0 (https://example.com/unlocked; contact@example.com)")
            connection.setRequestProperty("Accept", "application/sparql-results+json")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "SPARQL response: $response")

                val json = JSONObject(response)
                val results = json.getJSONObject("results").getJSONArray("bindings")

                if (results.length() > 0) {
                    val result = results.getJSONObject(0)

                    val area = result.optJSONObject("area")?.optString("value")?.toDoubleOrNull()
                    val population = result.optJSONObject("population")?.optString("value")?.toIntOrNull()
                    val elevation = result.optJSONObject("elevation")?.optString("value")?.toDoubleOrNull()

                    Log.d(TAG, "Parsed data - Area: $area, Population: $population, Elevation: $elevation")

                    WikidataResult(area, population, elevation)
                } else {
                    Log.w(TAG, "No results from SPARQL query")
                    null
                }
            } else {
                Log.e(TAG, "SPARQL query failed with code: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Wikidata: ${e.message}", e)
            null
        }
    }
}