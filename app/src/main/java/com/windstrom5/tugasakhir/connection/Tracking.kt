package com.windstrom5.tugasakhir.connection

import android.annotation.SuppressLint
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
import com.windstrom5.tugasakhir.activity.AbsensiActivity
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import org.json.JSONObject

class Tracking : Service() {
    private lateinit var locationManager: LocationManager
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            Log.d("latitude2", latitude.toString())
            sendLocationUpdateHandler(latitude, longitude, perusahaan, pekerja)
        }
    }

    private lateinit var handler: Handler
    private lateinit var locationUpdateRunnable: Runnable
    private lateinit var perusahaan: Perusahaan
    private lateinit var pekerja: Pekerja

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        perusahaan = intent?.getParcelableExtra("perusahaan")!!
        pekerja = intent.getParcelableExtra("pekerja")!!

        startLocationUpdates()
        startForegroundService()

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        handler = Handler(Looper.getMainLooper())
        locationUpdateRunnable = Runnable { startLocationUpdates() }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun startForegroundService() {
        createNotificationChannel()

        val notificationIntent = Intent(this, AbsensiActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Your App is running")
            .setContentText("Tracking location in the background")
            .setSmallIcon(R.mipmap.ic_logo)
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
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_INTERVAL,
                LOCATION_UPDATE_DISTANCE,
                locationListener
            )

            handler.postDelayed(locationUpdateRunnable, LOCATION_UPDATE_INTERVAL)
        } catch (ex: SecurityException) {
            Log.e(TAG, "Location permission not granted")
        }
    }

    private fun sendLocationUpdateHandler(latitude: Double, longitude: Double, perusahaan: Perusahaan, pekerja: Pekerja) {
        val url = "http://192.168.1.6:8000/api/UpdateLocation"
        val params = JSONObject().apply {
            put("perusahaan", perusahaan.nama)
            put("nama", pekerja.nama)
            put("latitude", latitude)
            put("longitude", longitude)
        }

        val request = JsonObjectRequest(Request.Method.PUT, url, params,
            { response ->
                try {
                    val status = response.getString("status")
                    val message = response.getString("message")
                    if (status == "success") {
                        // Handle success
                    } else {
                        // Handle error
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        handler.removeCallbacks(locationUpdateRunnable)
    }

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


