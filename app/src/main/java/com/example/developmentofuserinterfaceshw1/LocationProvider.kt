package com.example.developmentofuserinterfaceshw1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LocationProvider(private val activity: AppCompatActivity) {

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (permissionGranted) {
            onPermissionGranted?.invoke()
        }
    }

    private var permissionGranted = hasLocationPermission()
    private val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latestCurrentLocation = location
        }
    }

    private var latestCurrentLocation: Location? = null
    private var isTracking = false

    var onPermissionGranted: (() -> Unit)? = null

    fun requestPermissionIfNeeded() {
        if (hasLocationPermission()) {
            permissionGranted = true
            return
        }

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun startTracking() {
        if (!permissionGranted && !hasLocationPermission()) {
            return
        }

        permissionGranted = true
        isTracking = true
        requestUpdatesForAvailableProviders()
    }

    fun stopTracking() {
        if (!isTracking) {
            return
        }

        try {
            locationManager.removeUpdates(locationListener)
        } catch (exception: SecurityException) {
            // Permission may have been revoked while the app was running.
        }

        isTracking = false
    }

    fun getFreshLocationForScore(): Location? {
        return latestCurrentLocation ?: getBestLastKnownLocation()
    }

    private fun requestUpdatesForAvailableProviders() {
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        providers.forEach { provider ->
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                        provider,
                        LOCATION_UPDATE_INTERVAL_MS,
                        LOCATION_UPDATE_MIN_DISTANCE_METERS,
                        locationListener
                    )

                    locationManager.getCurrentLocation(
                        provider,
                        null,
                        activity.mainExecutor
                    ) { location ->
                        if (location != null) {
                            latestCurrentLocation = location
                        }
                    }
                }
            } catch (exception: SecurityException) {
                // Permission denied or revoked.
            } catch (exception: IllegalArgumentException) {
                // Provider unavailable on this device/emulator.
            }
        }
    }

    private fun getBestLastKnownLocation(): Location? {
        if (!permissionGranted && !hasLocationPermission()) {
            return null
        }

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        return providers
            .mapNotNull { provider ->
                try {
                    locationManager.getLastKnownLocation(provider)
                } catch (exception: SecurityException) {
                    null
                } catch (exception: IllegalArgumentException) {
                    null
                }
            }
            .maxByOrNull { it.time }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL_MS = 2_000L
        private const val LOCATION_UPDATE_MIN_DISTANCE_METERS = 0f
    }
}
