package com.example.project1

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Error
import java.util.concurrent.TimeUnit

class DetailManager {

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

    fun retrieveCategory(location: String, category: String, ctx: Context): List<Detail> {
        Log.d("DetailManager", "retrieveCategory() called")
        val radius = "1500"
        val request = Request.Builder()
            .url("https://api.yelp.com/v3/businesses/search?radius=$radius&location=$location&categories=$category&sort_by=rating")
            .header("Authorization", ctx.resources.getString(R.string.yelpKey))
            .build()

        Log.d("DetailManager", "before execute")

        val response = okHttpClient.newCall(request).execute() //blocks thread and waits, enqueue creates new thread

        Log.d("DetailManager", "after execute")


        // Parse response
        val foodattractionItems: MutableList<Detail> = mutableListOf()
        val responseString: String? = response.body?.string()
        if (response.isSuccessful && !responseString.isNullOrEmpty()){
            val json: JSONObject = JSONObject(responseString)
            val businesses: JSONArray = json.getJSONArray("businesses")
            for (i in 0 until businesses.length()){
                val curr = businesses.getJSONObject(i)
                val name = curr.getString("name")

                Log.d("DetailManager", "gets stuck after this")

                var price: String
                price = if (responseString.contains("price")){
                    curr.get("price").toString()
                } else {
                    "No Price"
                }

                Log.d("DetailManager", "gets stuck before this")

                val phone = curr.getString("phone")

                val location = curr.getJSONObject("location")
                val address1 = location.getString("address1")
                val address2 = location.getString("address2")
                val address3 = location.getString("address3")
                val address = "$address1, $address2, $address3"

                val rating = curr.getDouble("rating")
                val url = curr.getString("url")

                val coordinates = curr.getJSONObject("coordinates")
                val latitude = coordinates.getDouble("latitude")
                val longitude = coordinates.getDouble("longitude")


                val item = Detail (
                    name = name,
                    price = price,
                    address = address1,
                    rating = rating,
                    phone = phone,
                    url = url,
                    lat = latitude,
                    lon = longitude
                )

                // Add tweet
                foodattractionItems.add(item)

                Log.d("DetailManager", "added Restaurant $name")
            }

            Log.d("DetailManager", "finishes getting restaurants")
        }

        return foodattractionItems
    }

}