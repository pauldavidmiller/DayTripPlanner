package com.example.project1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.jetbrains.anko.doAsync
import java.lang.Exception
import java.text.DecimalFormat


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var progressBar: ProgressBar

    private lateinit var confirm : Button

    private lateinit var mMap: GoogleMap

    private lateinit var currentAddress: String

    val intent_details: MutableList<Detail> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        currentAddress = intent.getStringExtra(MainActivity.Constants.destination_intent)

        // Set progress bar running and hide details button until data is received
        progressBar = findViewById(R.id.progressBar)
        progressBar.isIndeterminate = true

        confirm = findViewById(R.id.confirm)
        confirm.setOnClickListener {
            if (currentAddress != null){
                val detailsIntent = Intent(this, DetailsActivity::class.java)
                detailsIntent.putExtra("address", currentAddress)
                detailsIntent.putExtra(MainActivity.Constants.foodSpinner_intent, intent.getStringExtra(MainActivity.Constants.foodSpinner_intent))
                detailsIntent.putExtra(MainActivity.Constants.attractionsSpinner_intent, intent.getStringExtra(MainActivity.Constants.attractionsSpinner_intent))
                detailsIntent.putExtra(MainActivity.Constants.foodSeekBar_intent, intent.getIntExtra(MainActivity.Constants.foodSeekBar_intent, 10))
                detailsIntent.putExtra(MainActivity.Constants.attractionsSeekBar_intent, intent.getIntExtra(MainActivity.Constants.attractionsSeekBar_intent, 10))
                detailsIntent.putParcelableArrayListExtra("details", ArrayList(intent_details))
                startActivity(detailsIntent)
            }
        }
        confirm.isEnabled = false
        confirm.visibility = View.INVISIBLE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Hold click and places marker
        //mMap.setOnMapLongClickListener { latLng: LatLng ->
            //Log.d("MapsActivity", "Long press at ${latLng.latitude}, ${latLng.longitude}")

            mMap.clear()

            // Do on a separate process
            doAsync {
                // Geocoder to get location of clicked lat and long
                val geocoder = Geocoder(this@MapsActivity)
                val results: List<Address> = try {
                    // Get destination from intent data
                    geocoder.getFromLocationName(intent.getStringExtra(MainActivity.Constants.destination_intent), 10)
                    /*geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        10
                    )*/
                } catch (exception: Exception){
                    exception.printStackTrace()
                    Log.e("MapsActivity", "Failed to retrieve results: $exception")
                    listOf<Address>()
                }


                // If there is at least one result
                if (results.isNotEmpty()){
                    Log.d("MapsActivity", "Received ${results.size} results")

                    // Get first address line
                    val firstResult = results.first()
                    val streetAddress = firstResult.getAddressLine(0)
                    Log.d("MapsActivity", "Destination at ${firstResult.latitude}, ${firstResult.longitude}")


                    // Create LatLng
                    val latLng: LatLng = LatLng(firstResult.latitude, firstResult.longitude)


                    // Marker of location and radius has to be placed on main thread
                    runOnUiThread {

                        // Place marker
                        val marker = MarkerOptions()
                            .position(latLng)
                            .title(streetAddress)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        mMap.addMarker(marker)


                        // Add radius
                        val circle: CircleOptions = CircleOptions()
                            .center(latLng)
                            .radius(1500.00)
                            .strokeColor(Color.RED)
                        mMap.addCircle(circle)


                        // Update Map Center
                        val builder = LatLngBounds.Builder()
                        builder.include(marker.getPosition())
                        val bounds = builder.build()

                        val padding = 0 // offset from edges of the map in pixels
                        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                        mMap.animateCamera(cu)
                    }

                    // Get closes metro station
                    val metroManager = MetroManager()
                    Log.d("MapsActivity", "about to call retrieveMetroStops")
                    try {
                        // Get Metro Stops given Long and Lat
                        val stops = metroManager.retrieveMetroStops(
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            ctx = this@MapsActivity
                        )

                        var shortestStop: MetroStop = stops[0]

                        // Need this or it goes to the white house
                        for (stop in stops){
                            if (calculationByDistance(latLng, LatLng(stop.lat, stop.lon)) <= calculationByDistance(latLng, LatLng(shortestStop.lat, shortestStop.lon))){
                                shortestStop = stop
                            }
                        }

                        // Get metro name through Station Information API
                        val stationName = metroManager.retrieveMetroNames(shortestStop.stationCode, this@MapsActivity)


                        runOnUiThread {
                            // Put marker down
                            // TODO still need to put .title by calling wmata station info
                            val metroMarker = MarkerOptions()
                                .position(LatLng(shortestStop.lat, shortestStop.lon))
                                .title(stationName)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            mMap.addMarker(metroMarker)
                        }

                    } catch (exception: Exception){
                        runOnUiThread {
                            Toast.makeText(this@MapsActivity, "No Metro Stops in Area", Toast.LENGTH_LONG).show()
                        }
                    }

                    // Initialize CategoryManager
                    val categoryManager = DetailManager()


                    // Get Restaurants
                    Log.d("MapsActivity", "about to call retrieveCategory for restaurants")
                    try {
                        val restaurants = categoryManager.retrieveCategory(
                            location = streetAddress,
                            category = intent.getStringExtra(MainActivity.Constants.foodSpinner_intent)!!,
                            ctx = this@MapsActivity
                        )

                        Log.d("MapsActivity", "finishes calling retrieveRestaurants")

                        runOnUiThread {
                            // Either use the seekbar number or max number returned
                            var lengthOfArray = restaurants.size
                            val foodSeekBar = intent.getIntExtra(MainActivity.Constants.foodSeekBar_intent, lengthOfArray)
                            if (lengthOfArray >= foodSeekBar){
                                lengthOfArray = foodSeekBar
                            } else {
                                // put toast to let user know
                                Toast.makeText(this@MapsActivity, "There isn't enough restaurant results", Toast.LENGTH_LONG).show()
                            }

                            // Place each restaurant marker on the map
                            for (i in 0 until lengthOfArray){
                                // Add to restaurants list
                                intent_details.add(restaurants[i])

                                val restaurantAddress = restaurants[i].address

                                Log.d("MapsActivity", "restaurant[$i] = $restaurantAddress")
                                val foodMarker = MarkerOptions()
                                    .position(LatLng(restaurants[i].lat, restaurants[i].lon))
                                    .title(restaurants[i].name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))

                                // Add marker to map
                                mMap.addMarker(foodMarker)
                            }
                        }
                    } catch (exceptions: Exception){
                        runOnUiThread {
                            Toast.makeText(this@MapsActivity, "No Restaurants in Area", Toast.LENGTH_LONG).show()
                        }
                    }

                    // Get Attractions
                    Log.d("MapsActivity", "about to call retrieveCategory for attractions")
                    try {
                        val attractions = categoryManager.retrieveCategory(
                            location = streetAddress,
                            category = intent.getStringExtra(MainActivity.Constants.attractionsSpinner_intent)!!,
                            ctx = this@MapsActivity
                        )

                        Log.d("MapsActivity", "finishes calling retrieveAttractions")

                        runOnUiThread {
                            // Either use the seekbar number or max number returned
                            var lengthOfArray = attractions.size
                            val attractionsSeekBar = intent.getIntExtra(MainActivity.Constants.attractionsSeekBar_intent, lengthOfArray)
                            if (lengthOfArray >= attractionsSeekBar){
                                lengthOfArray = attractionsSeekBar
                            } else {
                                // put toast to let the user know
                                Toast.makeText(this@MapsActivity, "There isn't enough attractions results", Toast.LENGTH_LONG).show()
                            }


                            // Place each marker on the map
                            for (i in 0 until lengthOfArray){
                                // Add attractions to details list
                                intent_details.add(attractions[i])

                                val attractionAddress = attractions[i].address

                                Log.d("MapsActivity", "attraction[$i] = $attractionAddress")
                                val attractionMarker = MarkerOptions()
                                    .position(LatLng(attractions[i].lat, attractions[i].lon))
                                    .title(attractions[i].name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                                // Add marker to map
                                mMap.addMarker(attractionMarker)
                            }
                        }
                    } catch (exception: Exception){
                        runOnUiThread {
                            Toast.makeText(this@MapsActivity, "No Attractions in Area", Toast.LENGTH_LONG).show()
                        }
                    }


                } else {
                    Log.d("MapsActivity", "No results")
                }

                runOnUiThread {
                    // Make details button enabled and visible and hide progress bar
                    confirm.isEnabled = true
                    confirm.visibility = View.VISIBLE

                    progressBar.isIndeterminate = false
                }
            }
        //}

        // Add a marker where clicked and give address and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


    //https://stackoverflow.com/questions/14394366/find-distance-between-two-points-on-map-using-google-map-api-v2
    private fun calculationByDistance(StartP: LatLng, EndP: LatLng): Double {
        val Radius = 6371// radius of earth in Km
        val lat1 = StartP.latitude
        val lat2 = EndP.latitude
        val lon1 = StartP.longitude
        val lon2 = EndP.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + (Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2))
        val c = 2 * Math.asin(Math.sqrt(a))
        val valueResult = Radius * c
        val km = valueResult / 1
        val newFormat = DecimalFormat("####")
        val kmInDec = Integer.valueOf(newFormat.format(km))
        val meter = valueResult % 1000
        val meterInDec = Integer.valueOf(newFormat.format(meter))
//        Log.i(
//            "Radius Value", "" + valueResult + "   KM  " + kmInDec
//                    + " Meter   " + meterInDec
//        )

        return Radius * c
    }

}