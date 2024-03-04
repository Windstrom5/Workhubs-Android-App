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
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.connection.LocationUpdate
import com.windstrom5.tugasakhir.connection.LocationWebSocketService
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.OkHttpClient
import org.json.JSONException
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

class TrackingFragment : Fragment() {
    private lateinit var webSocket: WebSocket
    private lateinit var mapView: MapView
    private lateinit var scarlet: Scarlet
    private lateinit var locationService: LocationWebSocketService
    private val markers = mutableMapOf<String, Marker>() // Map to store markers by user name
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var requestQueue: RequestQueue
    private var perusahaan : Perusahaan? = null
    private var pekerja : Admin? = null
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
        return view
    }
    private fun getBundle() {
        val arguments = arguments
        if (arguments != null) {
            perusahaan = arguments.getParcelable("perusahaan")
            pekerja = arguments.getParcelable("user")
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
    override fun onPause() {
        super.onPause()

        // Stop the periodic update task when the fragment is paused
        handler.removeCallbacks(updateRunnable)
    }
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Perform location update and marker update here
            perusahaan?.let { updateLocation(it) }

            // Repeat the task after the update interval
            handler.postDelayed(this, updateIntervalMillis)
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
    private fun updateLocation(perusahaan: Perusahaan) {
        // Send request to server to get user's locations
        val serverUrl = "your_server_url/api/getLocation/$perusahaan.nama"
        val request = JsonArrayRequest(
            Request.Method.GET, serverUrl, null,
            { response ->
                // Handle the response from the server
                try {
                    // Loop through the array of user locations
                    for (i in 0 until response.length()) {
                        val userObj = response.getJSONObject(i)
                        val userLatitude = userObj.getDouble("latitude")
                        val userLongitude = userObj.getDouble("longitude")
                        val nama = userObj.getString("nama")
                        val locationUpdate = LocationUpdate(nama, userLatitude, userLongitude)
                        updateMapMarker(locationUpdate)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Handle error
                error.printStackTrace()
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(request)
    }

    private fun updateMapMarker(locationUpdate: LocationUpdate) {
        val userName = locationUpdate.Nama
        val latitude = locationUpdate.latitude
        val longitude = locationUpdate.longitude

        // Use handler to run the API request on the background thread
        handler.post {
            updateMarkerWithApiRequest(userName, latitude, longitude)
        }
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
