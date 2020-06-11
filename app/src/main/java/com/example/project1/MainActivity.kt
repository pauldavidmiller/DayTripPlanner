package com.example.project1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    // Variables
    private lateinit var destination: EditText

    private lateinit var foodSpinner: Spinner
    private lateinit var attractionsSpinner: Spinner

    private lateinit var foodCount: TextView
    private lateinit var attractionsCount: TextView

    private lateinit var foodSeekBar: SeekBar
    private lateinit var attractionsSeekBar: SeekBar

    private lateinit var goButton: Button

    object Constants {
        const val destination_intent = "destination_intent"
        const val foodSpinner_intent = "foodSpinner_intent"
        const val attractionsSpinner_intent = "attractionsSpinner_intent"
        const val foodSeekBar_intent = "foodSeekBar_intent"
        const val attractionsSeekBar_intent = "attractionsSeekBar_intent"

        const val pref_destination = "pref_destination"
        const val pref_foodSpinner = "pref_foodSpinner"
        const val pref_attractionsSpinner = "pref_attractionsSpinner"
        const val pref_foodSeekBar = "pref_foodSeekBar"
        const val pref_attractionsSeekBar = "pref_attractionsSeekBar"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "onCreate() called")

        // Set Title
        title = "Day Trip Planner"

        val preferences: SharedPreferences = getSharedPreferences(
            "project1",
            Context.MODE_PRIVATE
        )

        // Initialize variables
        destination = findViewById(R.id.destinationInput)
        foodSpinner = findViewById(R.id.spinnerRestaurantCategories)
        attractionsSpinner = findViewById(R.id.spinnerAttractionCategories)
        foodCount = findViewById(R.id.numberResultsFood)
        attractionsCount = findViewById(R.id.numberResultsAttractions)
        foodSeekBar = findViewById(R.id.seekBarFood)
        attractionsSeekBar = findViewById(R.id.seekBarAttractions)
        goButton = findViewById(R.id.goButton)

        // Set options for spinner
        val foodSpinnerOptions = resources.getStringArray(R.array.foodOptions)
        val foodAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, foodSpinnerOptions)
        foodSpinner.adapter = foodAdapter
        val attractionsSpinnerOptions = resources.getStringArray(R.array.attractionsOptions)
        val attractionsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, attractionsSpinnerOptions)
        attractionsSpinner.adapter = attractionsAdapter

        // Set Listeners
        goButton.isEnabled = false // set to false initially
        destination.addTextChangedListener(textWatcher)
        foodSpinner.setOnItemSelectedListener(spinnerWatcher)
        attractionsSpinner.setOnItemSelectedListener(spinnerWatcher)
        foodSeekBar.setOnSeekBarChangeListener(foodSeekBarWatcher)
        attractionsSeekBar.setOnSeekBarChangeListener(attractionsSeekBarWatcher)

        // Set Preferences
        destination.setText(preferences.getString(Constants.pref_destination, ""))
        foodSpinner.setSelection(preferences.getInt(Constants.pref_foodSpinner, 0))
        attractionsSpinner.setSelection(preferences.getInt(Constants.pref_attractionsSpinner, 0))
        foodSeekBar.setProgress(preferences.getInt(Constants.pref_foodSeekBar, 3))
        attractionsSeekBar.setProgress(preferences.getInt(Constants.pref_attractionsSeekBar, 3))




        // Set Counts
        foodCount.setText(getString(R.string.foodCountString, foodSeekBar.progress.toString()))
        attractionsCount.setText(getString(R.string.attractionsCountString, attractionsSeekBar.progress.toString()))


        // Get destination preferences if saved before
//        val savedDestination: String? = preferences.getString("destination", "")
//        destination.setText(savedDestination)


        // Go button Listener
        goButton.setOnClickListener { //view: View ->
            Log.d("MainActivity", "onClick() called")


            /*// Create dialog with destination
            val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice)
            arrayAdapter.add(destination.text.toString())

            AlertDialog.Builder(this)
                .setTitle(R.string.search_results)
                .setAdapter(arrayAdapter) { _, _ ->
                    Toast.makeText(this, "You entered: ${destination.text}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()*/


            // Save user credentials to file
            val inputtedDestination = destination.text.toString()
            val inputtedFoodSpinnerPos = foodSpinner.selectedItemPosition
            val inputtedAttractionSpinnerPos = attractionsSpinner.selectedItemPosition
            val inputtedFoodSeekBar = foodSeekBar.progress
            val inputtedAttractionsSeekBar = attractionsSeekBar.progress


            //can encrypt here, but wont for this example ***

            // Set Preferences
            preferences
                .edit()
                .putString(Constants.pref_destination, inputtedDestination)
                .putInt(Constants.pref_foodSpinner, inputtedFoodSpinnerPos)
                .putInt(Constants.pref_attractionsSpinner, inputtedAttractionSpinnerPos)
                .putInt(Constants.pref_foodSeekBar, inputtedFoodSeekBar)
                .putInt(Constants.pref_attractionsSeekBar, inputtedAttractionsSeekBar)
                .apply()


            // Send to Map
            // need to use the actual yelp API format
            val inputtedFoodSpinner = foodSpinner.selectedItem.toString()
            val inputtedAttractionSpinner = attractionsSpinner.selectedItem.toString()


            val intent: Intent = Intent(this, MapsActivity::class.java) //go from here to MapActivity class
            intent.putExtra(Constants.destination_intent, inputtedDestination) //send data through the intent (string, string)
            intent.putExtra(Constants.foodSpinner_intent, inputtedFoodSpinner)
            intent.putExtra(Constants.attractionsSpinner_intent, inputtedAttractionSpinner)
            intent.putExtra(Constants.foodSeekBar_intent, inputtedFoodSeekBar)
            intent.putExtra(Constants.attractionsSeekBar_intent, inputtedAttractionsSeekBar)
            startActivity(intent) //execute intent

        }

    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume() called")
    }

    override fun onStop() {
        Log.d("MainActivity", "onStop() called")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy() called")
        super.onDestroy()
    }


    // Watchers and Listeners
    private val textWatcher = object: TextWatcher {
        //can take out ? nullable parameter values
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //buttonCheck.invoke()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            buttonCheck.invoke()
        }


    }

    private val spinnerWatcher = object: AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            goButton.isEnabled = false
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            buttonCheck.invoke()
        }

    }

    private val foodSeekBarWatcher = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            foodCount.setText(getString(R.string.foodCountString, seekBar.progress.toString()))
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    private val attractionsSeekBarWatcher = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            attractionsCount.setText(getString(R.string.attractionsCountString, seekBar!!.progress.toString()))
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    // Button Check Lambda
    val buttonCheck = {
        val inputtedDestination = destination.text.toString()
        val inputtedFoodSpinner = foodSpinner.selectedItemPosition
        val inputtedAttractionsSpinner = attractionsSpinner.selectedItemPosition

        val enable: Boolean = inputtedDestination.trim().isNotEmpty() && inputtedFoodSpinner >= 0 && inputtedAttractionsSpinner >= 0

        goButton.isEnabled = enable // or login.setEnabled(enable)
    }
}

