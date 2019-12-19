package com.priomkhan.mymaps

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener



class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit  var  mMap: GoogleMap
    private val mapIsReady = MutableLiveData<Boolean>()
    private val mLocationPermissionGranted = MutableLiveData<Boolean>()
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLatLng : LatLng




    companion object {
        const val ERROR_DIALOG_REQUEST: Int = 9001
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
        const val DEFAULT_ZOOM = 15F
        const val DEFAULT_LAT = 0.0
        const val DEFAULT_LNG = 0.0
        const val HOME_LAT = 43.761796
        const val HOME_LNG = -79.214930


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if(servicesOK()){
            setContentView(R.layout.activity_maps)
            initMap()
            Toast.makeText(this,"Loading Map", Toast.LENGTH_SHORT).show()
            // Construct a FusedLocationProviderClient.
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


            mapIsReady.observe(this, Observer {
                if(it){
                    Toast.makeText(this,"Ready To Map", Toast.LENGTH_LONG).show()
                    getLocationPermission()



                }

            })

            mLocationPermissionGranted.observe(this, Observer {
                if(it){
                    mMap.isMyLocationEnabled =true
                    getDeviceLocation()
                }else{
                    //gotoLocation(HOME_LAT, HOME_LNG, 15F)
                    gotoLocation(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_ZOOM)
                }

            })
        }else{
            setContentView(R.layout.activity_main)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit -> {
                // Initialize a new instance of
                val builder = AlertDialog.Builder(this@MainActivity)

                // Set the alert dialog title
                builder.setTitle("Stop")

                // Display a message on alert dialog
                builder.setMessage("Do You Really Want To Exit?")

                // Set a positive button and its click listener on alert dialog
                builder.setPositiveButton("YES") { _, _ ->
                    // Do something when user press the positive button
                    Toast.makeText(applicationContext, "Application Closed", Toast.LENGTH_SHORT)
                        .show()
                    finishAndRemoveTask()
                }


                // Display a negative button on alert dialog
                builder.setNegativeButton("No") { _, _ ->
                    Toast.makeText(applicationContext, "Continue", Toast.LENGTH_SHORT).show()
                }


                // Display a neutral button on alert dialog
                builder.setNeutralButton("Cancel") { _, _ ->
                    Toast.makeText(
                        applicationContext,
                        "You cancelled the dialog.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()

                // Display the alert dialog on app interface
                dialog.show()

                return true

            }
            else -> super.onOptionsItemSelected(item)
        }

    }


    //This function check if the mobile is connected to the Wifi or Mobile internet, however
    // it does not work above Android O.
    @Suppress("DEPRECATION")
    private fun networkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnectedOrConnecting ?: false
    }

    @Suppress("DEPRECATION")
    fun servicesOK():Boolean{
        if(networkAvailable()){
            val isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
            if(isAvailable == ConnectionResult.SUCCESS){
                return true

            }else if(GooglePlayServicesUtil.isUserRecoverableError(isAvailable)){
                val dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST)
                dialog.show()
                mapIsReady.postValue(false)
            }else{
                Toast.makeText(this,"Can't Connect To Mapping Service", Toast.LENGTH_SHORT).show()
                mapIsReady.postValue(false)
            }
        }else{
            Toast.makeText(this,"Can't Connect To Internet", Toast.LENGTH_SHORT).show()

        }
        return false
    }


    private fun initMap(){

            val mapFragment : SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapIsReady.postValue(true)
    }

    private fun gotoLocation(lat: Double, lng: Double, zoom: Float){
        val latlng = LatLng(lat,lng)
        val cameraUpdate : CameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, zoom)
        mMap.animateCamera(cameraUpdate)
        mMap.addMarker(
            MarkerOptions().position(latlng)
                .title("Position: $lat : $lng")
        )


    }


    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted.postValue(true)

        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode== PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted.postValue(true)

            }else{
                Toast.makeText(this,"Location Permission Denied", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    private fun getDeviceLocation() {
        /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted.value==true) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this, OnCompleteListener {
                    if(it.isSuccessful){
                        if(it.result!=null){
                            val lastLat = it.result!!.latitude
                            val lastLng = it.result!!.longitude
                            lastLatLng = LatLng(lastLat,lastLng)
                            gotoLocation(lastLatLng.latitude,lastLatLng.longitude,
                                DEFAULT_ZOOM)
                        }

                    }else{
                        Toast.makeText(this,"Current location is null. Using defaults.", Toast.LENGTH_LONG).show()
                        gotoLocation(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_ZOOM)
                    }

                })
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message?:"Error: Unable to get current location")
        }

    }


}

