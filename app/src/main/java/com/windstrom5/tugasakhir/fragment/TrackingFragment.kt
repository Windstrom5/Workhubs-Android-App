package com.windstrom5.tugasakhir.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.LocationUpdate
import com.windstrom5.tugasakhir.connection.LocationWebSocketService
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Perusahaan
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.sql.Date
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.windstrom5.tugasakhir.model.Pekerja
import org.json.JSONException

class TrackingFragment : Fragment() {
    private lateinit var webSocket: WebSocket
    private lateinit var mapView: MapView
    private lateinit var scarlet: Scarlet
    private lateinit var locationService: LocationWebSocketService
    private val markers = mutableMapOf<String, Marker>() // Map to store markers by user name
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var requestQueue: RequestQueue
    private var perusahaan : Perusahaan? = null
    private var admin : Admin? = null
    private val pekerjaList = mutableListOf<Pekerja>()
    private val updateIntervalMillis = 5000L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tracking, container, false)
        mapView = view.findViewById(R.id.mapView)
        getBundle()
        initializeMap()
        requestQueue = Volley.newRequestQueue(requireContext())
        perusahaan?.let { getPekerja(it) }
        val options = PusherOptions()
        options.setCluster("mt1") // Replace with your Pusher cluster
        val pusher = Pusher("7d7b48e62cefacc3b046", options)
        pusher.connect()
        for (pekerja in pekerjaList) {
            val channel = pusher.subscribe("location-updates.${pekerja.nama}")
            channel.bind("App\\Events\\LocationUpdated") { event ->
                val data = event.data
                val nama: String = data[0].toString()
                val latitude: Double = data[1].toString().toDouble()
                val longitude: Double = data[2].toString().toDouble()
                val updated_at: String = data[3].toString()
                val locationUpdate = LocationUpdate(nama, latitude, longitude, updated_at)
                updateMapMarker(locationUpdate)
            }
        }

        return view
    }

    private fun getPekerja(perusahaan: Perusahaan) {
        val apiUrl = "http://127.0.0.1:8000/api/getPekerja/${perusahaan.nama}"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { response ->
                try {
                    val pekerjaArray = response.getJSONArray("pekerja") // Assuming your response has a "pekerja" array
                    for (i in 0 until pekerjaArray.length()) {
                        val pekerjaObject = pekerjaArray.getJSONObject(i)
                        val pekerja = Pekerja(
                            null,
                            pekerjaObject.getInt("id_perusahaan"),
                            pekerjaObject.getString("email"),
                            pekerjaObject.getString("password"),
                            pekerjaObject.getString("nama"),
                            Date(pekerjaObject.getLong("tanggal_lahir")),
                            pekerjaObject.getString("profile")
                        )
                        pekerjaList.add(pekerja)
                    }

                    // Now the pekerjaList contains all the fetched Pekerja data
                    // You can use pekerjaList as needed
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Handle error cases
                error.printStackTrace()
            })

        requestQueue.add(jsonObjectRequest)
    }
    private fun getBundle() {
        val arguments = arguments
        if (arguments != null) {
            perusahaan = arguments.getParcelable("perusahaan")
            admin = arguments.getParcelable("user")
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
    private fun initializeMap() {
        Configuration.getInstance().load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()))
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.controller.setZoom(12.0)
        mapView.controller.setCenter(mapView.mapCenter)
        mapView.isTilesScaledToDpi = true
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(12.0)
        mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                // Handle tap on the map if needed
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // Handle long press on the map if needed
                return false
            }
        }))
        mapView.controller.animateTo(mapView.mapCenter)
        mapView.controller.setZoom(14.0)
    }

    private fun updateMapMarker(locationUpdate: LocationUpdate) {
        val userName = locationUpdate.Nama
        val latitude = locationUpdate.latitude
        val longitude = locationUpdate.longitude
        val geoPoint = GeoPoint(latitude, longitude)

        if (markers.containsKey(userName)) {
            // Update the position of the existing marker
            val marker = markers[userName]
            marker?.position = geoPoint
        } else {
            // If the marker for the user doesn't exist, create a new one
            val marker = Marker(mapView)
            marker.position = geoPoint
            marker.title = userName
            mapView.overlays.add(marker)
            markers[userName] = marker
        }

        mapView.invalidate()
    }
    private fun updateMarkerWithApiRequest(userName: String, latitude: Double, longitude: Double) {
        if (!markers.containsKey(userName)) {
            // If the marker for the user doesn't exist, create a new one
            val marker = Marker(mapView)
            marker.position = GeoPoint(latitude, longitude)
            marker.title = userName
            mapView.overlays.add(marker)
            markers[userName] = marker
        } else {
            // Update the position of the existing marker
            val marker = markers[userName]
            marker?.position = GeoPoint(latitude, longitude)
        }
        mapView.invalidate()
    }
//    private fun updateLocation(perusahaan: Perusahaan) {
//        // Send request to server to get user's locations
//        val serverUrl = "https://1a9f-36-80-222-40.ngrok-free.app/api/getLocation/$perusahaan.nama"
//        val request = JsonArrayRequest(
//            Request.Method.GET, serverUrl, null,
//            { response ->
//                // Handle the response from the server
//                try {
//                    // Loop through the array of user locations
//                    for (i in 0 until response.length()) {
//                        val userObj = response.getJSONObject(i)
//                        val userLatitude = userObj.getDouble("latitude")
//                        val userLongitude = userObj.getDouble("longitude")
//                        val nama = userObj.getString("nama")
//                        val locationUpdate = LocationUpdate(nama, userLatitude, userLongitude)
//                        updateMapMarker(locationUpdate)
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            },
//            { error ->
//                // Handle error
//                error.printStackTrace()
//            }
//        )
//
//        // Add the request to the RequestQueue
//        requestQueue.add(request)
//    }
//    override fun onPause() {
//        super.onPause()
//
//        // Stop the periodic update task when the fragment is paused
//        handler.removeCallbacks(updateRunnable)
//    }
//    private val updateRunnable = object : Runnable {
//        override fun run() {
//            // Perform location update and marker update here
//            perusahaan?.let { updateLocation(it) }
//
//            // Repeat the task after the update interval
//            handler.postDelayed(this, updateIntervalMillis)
//        }
//    }
//        initializeWebSocket()
//    override fun onDestroy() {
//        super.onDestroy()
//    }

    //    private fun initializeWebSocket() {
//        val ngrokUrl = "https://1490-36-80-222-40.ngrok-free.app"
//        val webSocketUrl = "$ngrokUrl/broadcasting"
//
//        val okHttpClient = OkHttpClient.Builder().build()
//        scarlet = Scarlet.Builder()
//            .webSocketFactory(okHttpClient.newWebSocketFactory(webSocketUrl))
//            .addMessageAdapterFactory(MoshiMessageAdapter.Factory())
//            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
//            .build()
//
//        locationService = scarlet.create<LocationWebSocketService>()
//
//        locationService.observeLocationUpdates()
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { locationUpdate ->
//                updateMapMarker(locationUpdate)
//            }
//    }
}
