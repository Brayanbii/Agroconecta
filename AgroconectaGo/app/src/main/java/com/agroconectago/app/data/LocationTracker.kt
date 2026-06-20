package com.agroconectago.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat

class LocationTracker(private val context: Context) {

    private var locationManager: LocationManager? = null
    private var currentLat: Double? = null
    private var currentLng: Double? = null

    fun start() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return

        // Intentar obtener ultima ubicacion conocida inmediatamente
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = if (gpsLoc != null && netLoc != null) {
                if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
            } else gpsLoc ?: netLoc
            if (best != null) {
                currentLat = best.latitude
                currentLng = best.longitude
            }

            // Escuchar actualizaciones cada 1s / 10m
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    currentLat = loc.latitude
                    currentLng = loc.longitude
                }
                override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                override fun onProviderEnabled(p: String) {}
                override fun onProviderDisabled(p: String) {}
            }
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10f, listener)
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 20f, listener)
        }
    }

    fun getCurrentLocation(): Pair<Double, Double>? {
        val lat = currentLat
        val lng = currentLng
        return if (lat != null && lng != null) Pair(lat, lng) else null
    }
}
