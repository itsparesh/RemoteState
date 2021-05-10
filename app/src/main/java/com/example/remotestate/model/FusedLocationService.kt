package com.example.remotestate.model

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat

internal class FusedLocationService : Service() {
    private var mLocationManager: LocationManager? = null

    private class LocationListener(provider: String) : android.location.LocationListener {
        var mLastLocation: Location
        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")
            mLastLocation.set(location)
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
        }
    }

    /*
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    */
    private var mLocationListeners = arrayOf(
        LocationListener(LocationManager.PASSIVE_PROVIDER)
    )

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        initializeLocationManager()
        try {
            mLocationManager?.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                LOCATION_INTERVAL.toLong(),
                LOCATION_DISTANCE,
                mLocationListeners[0]
            )
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        /*try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[1]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }*/
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        if (mLocationManager != null) {
            for (mLocationListener in mLocationListeners) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mLocationManager?.removeUpdates(mLocationListener)
                } catch (ex: Exception) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex)
                }
            }
        }
    }

    private fun initializeLocationManager() {
        Log.e(
            TAG,
            "initializeLocationManager - LOCATION_INTERVAL: $LOCATION_INTERVAL LOCATION_DISTANCE: $LOCATION_DISTANCE"
        )
        if (mLocationManager == null) {
            mLocationManager =
                applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        }
    }

    companion object {
        private const val TAG = "MyLocationService"
        private const val LOCATION_INTERVAL = 1000
        private const val LOCATION_DISTANCE = 10f
    }
}