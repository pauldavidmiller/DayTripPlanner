package com.example.project1

import android.content.Context
import android.content.res.Resources
import android.provider.Settings.Global.getString
import android.util.Log
import com.example.project1.MetroStop
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MetroManager {

    private val okHttpClient: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()

        // Get logs from network traffic through interceptor
        // Set up our OkHttpClient instance to log all network traffic to LogCat
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        // Timeout if wait to long
        builder.connectTimeout(15, TimeUnit.SECONDS)
        builder.readTimeout(15, TimeUnit.SECONDS)

        okHttpClient = builder.build()
    }

    fun retrieveMetroNames(stationCode: String, ctx: Context): String {
        Log.d("MetroManager", "retrieveMetroNames() called")

        val request = Request.Builder()
            .url("https://api.wmata.com/Rail.svc/json/jStationInfo?StationCode=$stationCode")
            .header("api_key", ctx.resources.getString(R.string.metroKey))
            .build()

        Log.d("MetroManager", "Before execute")

        val response = okHttpClient.newCall(request).execute() //blocks thread and waits, enqueue creates new thread

        Log.d("MetroManager", "After execute: $response")

        // Parse response
        val responseString: String? = response.body?.string()
        if (response.isSuccessful && !responseString.isNullOrEmpty()){
            val json: JSONObject = JSONObject(responseString)
            return json.getString("Name")
        }

        return ""
    }

    fun retrieveMetroStops(latitude: Double, longitude: Double, ctx: Context): List<MetroStop> {
        Log.d("MetroManager", "retrieveMetroStops() called")
        val radius = "1500"

        val request = Request.Builder()
            .url("https://api.wmata.com/Rail.svc/json/jStationEntrances?latitude=$latitude&longitude=$longitude&radius=$radius")
            .header("api_key", ctx.resources.getString(R.string.metroKey))
            .build()



        Log.d("MetroManager", "Before execute")

        val response = okHttpClient.newCall(request).execute() //blocks thread and waits, enqueue creates new thread

        Log.d("MetroManager", "After execute: $response")


        // Parse response
        val stops: MutableList<MetroStop> = mutableListOf()
        val responseString: String? = response.body?.string()
        if (response.isSuccessful && !responseString.isNullOrEmpty()){
            val json: JSONObject = JSONObject(responseString)
            val entrances: JSONArray = json.getJSONArray("Entrances")
            for (i in 0 until entrances.length()){
                val curr = entrances.getJSONObject(i)
                val lat = curr.getString("Lat").toDouble()
                val lon = curr.getString("Lon").toDouble()
                val stationCode = curr.getString("StationCode1")

                val stop = MetroStop(
                    lat = lat,
                    lon = lon,
                    stationCode = stationCode
                )

                // Add tweet
                stops.add(stop)
            }
        }

        return stops
    }

}