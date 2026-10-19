package com.example.and_sales_portal.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/** A place the user must be near: a toko (visit), or an office or toko (attendance, revision 2026-10-08). */
data class Place(val lat: Double, val lng: Double, val address: String?, val name: String? = null)

sealed interface LocationResult {
    /** [accuracy]: GPS accuracy radius in metres (decision 2026-10-19: worse than 100 m is refused); `null` = unknown. */
    data class Found(val lat: Double, val lng: Double, val address: String?, val mock: Boolean, val accuracy: Float? = null) : LocationResult
    /** Permission not granted (the screen asks for it first). */
    data object PermissionDenied : LocationResult
    /** Location (GPS) is switched off on the device. */
    data object GpsOff : LocationResult
    /** No fix in time. */
    data object Unavailable : LocationResult
}

/** Location step of ATT flows (PRD D2 "map, coordinates, address"; D3 "GPS match with the toko lat/long"). */
class LocationService(private val context: Context, private val simulation: SimulationSettings) {

    fun hasPermission(): Boolean = PERMISSIONS.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    fun isEnabled(): Boolean = LocationManagerCompat.isLocationEnabled(context.getSystemService(LocationManager::class.java))

    suspend fun current(reference: Place): LocationResult {
        if (!hasPermission()) return LocationResult.PermissionDenied
        if (!isEnabled()) return LocationResult.GpsOff
        val sim = simulation.value
        return when (sim.location) {
            LocationMode.AT_STORE -> LocationResult.Found(reference.lat + JITTER, reference.lng - JITTER, reference.address, sim.mockLocation, GOOD_ACCURACY)
            LocationMode.OUT_OF_RADIUS -> LocationResult.Found(reference.lat + OUT_OF_RADIUS, reference.lng + OUT_OF_RADIUS, null, sim.mockLocation, GOOD_ACCURACY)
            LocationMode.LOW_ACCURACY -> LocationResult.Found(reference.lat + JITTER, reference.lng - JITTER, reference.address, sim.mockLocation, LOW_ACCURACY)
            LocationMode.DEVICE -> {
                val fix = deviceLocation() ?: return LocationResult.Unavailable
                LocationResult.Found(fix.latitude, fix.longitude, addressOf(fix.latitude, fix.longitude), sim.mockLocation || fix.isMockLocation(), fix.accuracy.takeIf { fix.hasAccuracy() })
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun deviceLocation(): Location? = withTimeoutOrNull(TIMEOUT_MS) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val token = CancellationTokenSource()
        suspendCancellableCoroutine<Location?> { cont ->
            cont.invokeOnCancellation { token.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) cont.resume(loc)
                    else client.lastLocation.addOnSuccessListener { cont.resume(it) }.addOnFailureListener { cont.resume(null) }
                }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun addressOf(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        runCatching { Geocoder(context, Locale("id", "ID")).getFromLocation(lat, lng, 1)?.firstOrNull()?.getAddressLine(0) }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun Location.isMockLocation() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isMock else isFromMockProvider

    companion object {
        val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        /** Distance in metres (WGS84). */
        fun distance(aLat: Double, aLng: Double, bLat: Double, bLng: Double): Float =
            FloatArray(1).also { Location.distanceBetween(aLat, aLng, bLat, bLng, it) }[0]

        private const val TIMEOUT_MS = 10_000L
        /** About 10 m: a GPS fix is never exactly on the toko. */
        private const val JITTER = 0.00007
        /** About 9 km north-east: outside the 3 km radius of every office and toko of the area. */
        private const val OUT_OF_RADIUS = 0.06
        /** Simulated accuracy radii: a good fix, and one worse than the 100 m limit. */
        private const val GOOD_ACCURACY = 8f
        private const val LOW_ACCURACY = 240f
    }
}
