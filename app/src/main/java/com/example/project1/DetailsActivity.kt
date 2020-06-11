package com.example.project1

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync
import java.lang.Exception


class DetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    object Constants {
        const val address = "address"
        const val details = "details"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details) //set tweets layout

        //retrieve intent information
        //val currentLocation: String? = intent.getStringExtra("LOCATION")
        val currentAddress: String? = intent.getStringExtra(Constants.address)

        title = currentAddress

        recyclerView = findViewById(R.id.recyclerView)

        //set the recyclerView direction to vertical (the default)
        recyclerView.layoutManager = LinearLayoutManager(this)

        doAsync {
            try {
                Log.d("DetailsActivity", "getDetails()")

                // Get details from intent
                val details: ArrayList<Detail> = intent.getParcelableArrayListExtra(Constants.details)

                runOnUiThread {
                    val adapter = DetailAdapter(details.toList(), this@DetailsActivity)
                    recyclerView.adapter = adapter

                    // If there are no details
                    if (details.size == 0){
                        Toast.makeText(this@DetailsActivity, "No Details to be Displayed", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (exception: Exception){
                runOnUiThread {
                    Toast.makeText(this@DetailsActivity, "failed to retrieve Details", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}