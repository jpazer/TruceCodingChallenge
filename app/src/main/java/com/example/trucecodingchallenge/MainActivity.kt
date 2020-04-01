package com.example.trucecodingchallenge

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream
import java.lang.reflect.Method
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var timer: Timer
    private lateinit var gpsData : MutableList<List<String>>
    private var gpsRow  = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize the location manager
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //create the gps test provider and enable it
        locationManager.addTestProvider("gps", false, true, false, false, true, true, true, 3, 1);
        locationManager.setTestProviderEnabled("gps", true)

        //parse the csv with gps spoof data
        parseGPSData()

    }
     fun startGPS(view: View){
         //update status text
         statusText.text = "Running"

         //set the coordinates text with the current coordinates
         fun setText(gpsRow:Int){
             coordinates.text = "["+ gpsData[gpsRow][0] +", "+ gpsData[gpsRow][0] + "]"
         }

         //create a 1 sec timer to change the location each second based on the gps data from the file
         timer = Timer()
         val task = object: TimerTask() {
             override fun run() {

                 // Create a new Location to inject into Location Services
                 val newLocation = Location("gps")
                 newLocation.latitude = gpsData[gpsRow][0].toDouble()
                 newLocation.longitude = gpsData[gpsRow][1].toDouble()
                 newLocation.bearing = gpsData[gpsRow][2].toFloat()
                 newLocation.speed = gpsData[gpsRow][3].toFloat()
                 newLocation.accuracy = gpsData[gpsRow][4].toFloat()
                 newLocation.time = System.currentTimeMillis()

                 //fix Jellybean error that does not accept a location object without ElapsedRealtimeNanos
                 val locationJellyBeanFixMethod: Method =
                     Location::class.java.getMethod("makeComplete")
                 if (locationJellyBeanFixMethod != null) {
                     locationJellyBeanFixMethod.invoke(newLocation)
                 }

                 //set the location
                 locationManager.setTestProviderLocation("gps", newLocation)

                 //set the coordinates text from outside the loop
                 setText(gpsRow)

                 //increment the row, or reset to zero at end of data
                 if (gpsRow >= gpsData.size - 1){
                     gpsRow = 0
                 }else{
                     gpsRow++
                 }
                println(gpsRow)
             }
         }
         timer.schedule(task, 0, 1000)
    }

     fun stopGPS(view: View) {
         //pause the timer
         timer.cancel()

         //update status text
         statusText.text = "Paused"
     }

    fun openMaps(view: View){
        //open the google maps app
        val uri = "http://maps.google.com/maps"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }

    fun parseGPSData(){
        //get the file of gps data and put it into a list
        gpsData = mutableListOf()
        val file: InputStream = assets.open("gps.txt")
        csvReader().open(file) {
            readAllAsSequence().forEach { row ->
                gpsData.add(row)
            }
        }

    }
}
