package com.example.remotestate.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remotestate.model.FusedLocationService
import com.example.remotestate.R
import com.example.remotestate.model.SaveData
import com.example.remotestate.model.SaveDataDatabase
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var mMap: GoogleMap? = null
    private var fusedLocationService: FusedLocationService? = null
    private var locationManager: LocationManager? = null
    var delay: Long = 30 * 1000 //Delay for 30 seconds.
    private lateinit var adapter: ListAdapter
    private var locationList: List<SaveData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showLocationPrompt()
        requestPermission()
        loadMap()
        setOnClickListener()
        setRecyclerView()
    }

    private fun setRecyclerView() {
        locationRV?.layoutManager = LinearLayoutManager(this)
        adapter = ListAdapter(this, object : ListAdapter.OnListItemClick {
            override fun onListItemClicked(clickedItem: SaveData?) {
                clickedItem?.latitude?.let {
                    loadPointsOnMap(it, clickedItem.longitude)
                }
            }
        })
        locationRV?.addItemDecoration(
            DividerItemDecoration(
                locationRV?.context,
                (locationRV?.layoutManager as LinearLayoutManager).orientation
            )
        )
        locationRV?.adapter = adapter
    }

    private fun setOnClickListener() {
        startBtn?.setOnClickListener {
            /*if (fusedLocationService == null) {
                fusedLocationService = FusedLocationService()
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, FusedLocationService::class.java))
            } else {
                startService(Intent(this, FusedLocationService::class.java))
            }*/
            if (checkPermission()) {
                mMap?.isMyLocationEnabled = true
            }
            getLocation()
            RepeatHelper.repeatDelayed(delay) {
                RepeatHelper.repeatDelayed(delay) { getLocation() }
            }
        }
        stopBtn?.setOnClickListener {
            if (checkPermission()) {
                mMap?.isMyLocationEnabled = false
            }
            //stopService(Intent(this, FusedLocationService::class.java))
            runnable?.let { it1 -> handler?.removeCallbacks(it1) }
        }

        listBtn?.setOnClickListener {
            locationRV?.visibility = View.VISIBLE
            mapLL?.visibility = View.GONE
            saveInDB(Double.MIN_VALUE, Double.MIN_VALUE, "")
        }
    }

    private fun getLocation() {
        if (checkPermission()) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
            val locationGPS = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (locationGPS != null) {
                val lat = locationGPS.latitude
                val long = locationGPS.longitude
                loadPointsOnMap(lat, long)
                saveInDB(lat, long, System.currentTimeMillis().toString())
                Log.e("ChangeP", "Your Location: \nLatitude: $lat\nLongitude: $long")
            } else {
                Log.e( "ChangeP","Unable to find location.")
                getLocation()
            }
        }
    }

    private fun loadMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            mMap = googleMap
            mMap?.setOnMapLoadedCallback {
                mMap = googleMap
                initMapStuff()
            }
        }
    }

    private fun loadPointsOnMap(lat: Double = 55.3781, long: Double = -3.4360) {
        mapLL?.visibility = View.VISIBLE
        locationRV?.visibility = View.GONE
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, long), 15f))
    }

    private fun initMapStuff() {
        loadPointsOnMap()
        if (checkPermission()) {
            mMap?.isMyLocationEnabled = true
            mMap?.uiSettings?.isMyLocationButtonEnabled = true
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                val fineLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val coarseLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (fineLocation && coarseLocation) {
                    if (checkPermission()) {
                        if (mMap != null) {
                            loadMap()
                        }
                    }
                } else {
                    Toast.makeText(this, "Location is disabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        val firstPermissionResult = ContextCompat.checkSelfPermission(
            this.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val secondPermissionResult = ContextCompat.checkSelfPermission(
            this.applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return firstPermissionResult == PackageManager.PERMISSION_GRANTED && secondPermissionResult == PackageManager.PERMISSION_GRANTED
    }

    private fun showLocationPrompt() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this@MainActivity)
                .checkLocationSettings(builder.build())

        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val resolvable: ResolvableApiException =
                                exception as ResolvableApiException
                            resolvable.startResolutionForResult(
                                this@MainActivity, LocationRequest.PRIORITY_HIGH_ACCURACY
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    private fun saveInDB(lat: Double, long: Double, time: String) {
        dbHandler = Handler(Looper.getMainLooper())
        threadRunnable = Runnable {
            val saveDataDatabase = SaveDataDatabase.getDatabase(this@MainActivity)
            if (lat != Double.MIN_VALUE && long != Double.MIN_VALUE) {
                saveDataDatabase.saveDataDao().insertSaveData(SaveData(lat, long, time))
            }
            locationList = saveDataDatabase.saveDataDao().getSaveData()
            renderList(locationList)
            if (!locationList.isNullOrEmpty()) {
                Log.e(
                    "ChangeP",
                    "Item = " + locationList?.get(0)?.latitude + " with time = " + locationList?.get(0)?.timeStamp
                )
            } else {
                Log.e("ChangeP", "NULL")
            }
        }
        dbHandler?.postDelayed(threadRunnable as Runnable, 0)
    }

    companion object {
        private const val PERMISSION_CODE = 23
        private var dbHandler: Handler? = null
        private var handler: Handler? = null
        private var threadRunnable: Runnable? = null
        private var runnable: Runnable? = null
    }

    object RepeatHelper {
        fun repeatDelayed(delay: Long, todo: () -> Unit) {
            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    todo()
                    handler?.postDelayed(this, delay)
                }
            }
            handler?.postDelayed(runnable as Runnable, delay)
        }
    }

    private fun renderList(locationList: List<SaveData>?) {
        locationList?.let { adapter.addData(it.toMutableList()) }
        adapter.notifyDataSetChanged()
    }
}