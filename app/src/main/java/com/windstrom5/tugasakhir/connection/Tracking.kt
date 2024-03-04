package com.windstrom5.tugasakhir.connection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.windstrom5.tugasakhir.R
import org.json.JSONObject

class Tracking : Service() {
    private lateinit var locationManager: LocationManager
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Handle location updates
            val latitude = location.latitude
            val longitude = location.longitude
            sendLocationUpdateHandler(latitude, longitude)
        }

    }

    private lateinit var handler: Handler
    private lateinit var locationUpdateRunnable: Runnable

    override fun onBind(intent: Intent?): IBinder? {
        // Return null because we don't intend to allow binding to this service
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Initialize Handler and Runnable for periodic location updates
        handler = Handler(Looper.getMainLooper())
        locationUpdateRunnable = Runnable { startLocationUpdates() }

        // Start listening for location updates
        startLocationUpdates()
        // Make the service a foreground service
        startForegroundService()
    }
    private fun startForegroundService() {
        // Create a notification channel (for Android Oreo and higher)
        createNotificationChannel()

        // Create a notification intent
        val notificationIntent: Intent? = null
        val pendingIntent = notificationIntent?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        // Create a notification
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Your App is running")
            .setContentText("Tracking location in the background")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Your App Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startLocationUpdates() {
        try {
            // Request location updates from the GPS provider with a specified interval and distance criteria
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_INTERVAL,
                LOCATION_UPDATE_DISTANCE,
                locationListener
            )

            // Schedule the next location update after the specified interval
            handler.postDelayed(locationUpdateRunnable, LOCATION_UPDATE_INTERVAL)
        } catch (ex: SecurityException) {
            Log.e(TAG, "Location permission not granted")
        }
    }

    private fun sendLocationUpdateHandler(latitude: Double, longitude: Double) {
        // Implement logic to send location update to the server using Volley
        val url = "your_server_url/absen"
        val params = JSONObject()
        try {
            params.put("latitude", latitude)
            params.put("longitude", longitude)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                // Handle the response from the server
                try {
                    val status = response.getString("status")
                    val message = response.getString("message")

                    // Process the status and message accordingly
                    if ("success" == status) {
                        // Handle success
                    } else {
                        // Handle error
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Handle error
                error.printStackTrace()
            }
        )

        // Add the request to the request queue
        Volley.newRequestQueue(this).add(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when the service is destroyed
        locationManager.removeUpdates(locationListener)

        // Remove any pending callbacks to avoid leaks
        handler.removeCallbacks(locationUpdateRunnable)
    }

    // Other Service lifecycle methods...

    companion object {
        private const val TAG = "BackgroundService"
        private const val LOCATION_UPDATE_INTERVAL: Long = 10000 // 10 seconds
        private const val LOCATION_UPDATE_DISTANCE: Float = 10f // 10 meters
        private const val NOTIFICATION_ID = 12345 // Use any unique ID for your notification
        private const val CHANNEL_ID = "LocationUpdateServiceChannel"
    }
}


//    private lateinit var locationManager: LocationManager
//    private val locationListener: LocationListener = object : LocationListener {
//        override fun onLocationChanged(location: Location) {
//            // Handle location updates
//            val latitude = location.latitude
//            val longitude = location.longitude
//
//            // Update your UI or perform any other actions with the real-time location data
//
//            // Send location update to the server via WebSocket or any other mechanism
//            sendLocationUpdate(latitude, longitude)
//        }
//
//        // Other LocationListener methods...
//
//    }
//    override fun onBind(intent: Intent?): IBinder? {
//        // Return null because we don't intend to allow binding to this service
//        return null
//    }
//    override fun onCreate() {
//        super.onCreate()
//        // Initialize LocationManager
//        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//
//        // Start listening for location updates
//        startLocationUpdates()
//    }
//
//    private fun startLocationUpdates() {
//        try {
//            // Request location updates from the GPS provider with a specified interval and distance criteria
//            locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                LOCATION_UPDATE_INTERVAL,
//                LOCATION_UPDATE_DISTANCE,
//                locationListener
//            )
//        } catch (ex: SecurityException) {
//            Log.e(TAG, "Location permission not granted")
//        }
//    }
//
//    private fun sendLocationUpdate(latitude: Double, longitude: Double) {
//        // Implement logic to send location update to the server
//        // You can use WebSocket, Retrofit, or any other networking library
//        // Example: WebSocketService().sendLocationUpdate(latitude, longitude)
//    }
//
//    // Other Service lifecycle methods...
//
//    companion object {
//        private const val TAG = "LocationUpdateService"
//        private const val LOCATION_UPDATE_INTERVAL: Long = 10000 // 10 seconds
//        private const val LOCATION_UPDATE_DISTANCE: Float = 10f // 10 meters
//        private const val NOTIFICATION_ID = 12345 // Use any unique ID for your notification
//        private const val CHANNEL_ID = "LocationUpdateServiceChannel"
//    }
//}


