package com.example.tugasakhir

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import com.example.tugasakhir.databinding.ActivityMapBinding
import com.example.tugasakhir.location.CustomInfoWindow
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import android.location.Address as AndroidLocationAddress

class MapActivity : AppCompatActivity(){
    private lateinit var mapController: IMapController
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var mapView: MapView
    private lateinit var bundle : Bundle
    private lateinit var binding: ActivityMapBinding
    private lateinit var save : ExtendedFloatingActionButton
    private lateinit var myLocationMarker: Marker
    private lateinit var locationManager: LocationManager
    private lateinit var tvLatitudeLongitude : TextView
    private lateinit var selectedLocationMarker: Marker
    private lateinit var tvAddress : TextView
    private lateinit var defaultMarker: Marker
    private var latitude : Double = 0.0
    private var longitude : Double = 0.0
    private lateinit var namaPerusahaan : String
    private lateinit var address : String
    lateinit var tvPerusahaan : TextView
    private lateinit var cardview : CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Configuration.getInstance().userAgentValue = packageName
        save = binding.saveButton
        tvLatitudeLongitude = binding.tvLatitudeLongitude
        tvAddress = binding.tvAddress
        save.visibility = View.INVISIBLE
        mapView = binding.mapView
        getBundle()
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(false)
        mapView.setMultiTouchControls(true)
        cardview = binding.infoCardView
        cardview.visibility = View.INVISIBLE
        mapController = mapView.controller
        mapController.setZoom(20.0)
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        mapView.overlays.add(locationOverlay)
        locationOverlay.enableMyLocation()
        selectedLocationMarker = Marker(mapView)
        myLocationMarker = Marker(mapView)
        defaultMarker = Marker(mapView)
        defaultMarker.title = "Selected Location"
        myLocationMarker.icon = resources.getDrawable(R.drawable.location) // Replace with your person logo drawable
        myLocationMarker.title = "My Location"
        val myLocationInfoWindow = CustomInfoWindow(R.layout.custom_info_window, mapView, myLocationMarker.title,myLocationMarker)
        myLocationMarker.infoWindow = myLocationInfoWindow
        mapView.overlays.add(myLocationMarker)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                locationListener
            )

            // Get last known location
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (lastKnownLocation != null) {
                // Center the map on the last known location
                val userLocation = GeoPoint(lastKnownLocation)
                mapController.animateTo(userLocation)

                // Add a marker at the user's location
                myLocationMarker.position = userLocation
                mapView.invalidate() // Refresh the map to update the marker position

                // Get the map center
                val mapCenter = mapView.mapCenter
                // Add a marker at the center of the screen
                selectedLocationMarker.position = GeoPoint(mapCenter.latitude, mapCenter.longitude)
                mapView.invalidate() // Refresh the map to update the marker position
            }
        }
        save.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            val Bundle = Bundle()
            Bundle.putString("latitude", namaPerusahaan)
            Bundle.putDouble("latitude", latitude) // Serialize the user object to a Bundle
            Bundle.putDouble("longitude", longitude)
            Bundle.putString("address", address)
            intent.putExtra("data", bundle)
            startActivity(intent)
//            finish()
//            // Handle save button click
//            // You can retrieve latitude, longitude, and address here
//            val latitude = mapView.boundingBox.centerLatitude
//            val longitude = mapView.boundingBox.centerLongitude
//            // Remove existing marker if present
//            selectedLocationMarker = Marker(mapView)
//            selectedLocationMarker.let { mapView.overlays.remove(it) }
//            // Add a marker to the selected location
//            selectedLocationMarker.position = GeoPoint(latitude, longitude)
//            selectedLocationMarker.title = "Selected Location"
//            val selectedLocationInfoWindow = CustomInfoWindow(R.layout.custom_info_window, mapView, selectedLocationMarker.title, selectedLocationMarker)
//            selectedLocationMarker.infoWindow = selectedLocationInfoWindow
//
//            mapView.overlays.add(selectedLocationMarker)
        }
        // Set up the map click listener to make the save button visible
        mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    // User clicked on the map, make Save button and CardView visible
                    save.visibility = View.VISIBLE
                    cardview.visibility = View.VISIBLE
                    latitude = it.latitude
                    longitude = it.longitude
                    // Fetch and display address information
                    getAddressFromLocation(it.latitude, it.longitude)

                    // Remove existing markers
//                    mapView.overlays.remove(myLocationMarker)
                    mapView.overlays.remove(selectedLocationMarker)

                    // Add a marker to the selected location
                    selectedLocationMarker.position = GeoPoint(it.latitude, it.longitude)
                    selectedLocationMarker.title = "Selected Location"
                    mapView.overlays.add(selectedLocationMarker)

                    // Move the default marker to the tapped location
                    defaultMarker.position = GeoPoint(it.latitude, it.longitude)
                    mapView.invalidate()

                    return true
                }
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // Handle long press if needed
                return false
            }
        }))
    }
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Move map to the new location
            mapController.animateTo(GeoPoint(location))
        }

        override fun onProviderDisabled(provider: String) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<AndroidLocationAddress> = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                val Address: AndroidLocationAddress = addresses[0]
                address = Address.getAddressLine(0)
                tvLatitudeLongitude.text = "Latitude: $latitude, Longitude: $longitude"
                tvAddress.text = "Address: $Address"
                tvPerusahaan.text = namaPerusahaan
            } else {
                tvAddress.text = "Address: Not Available"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            tvAddress.text = "Address: Not Available"
        }
    }
    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")!!
        bundle.let {
            namaPerusahaan = it.getString("namaperusahaan").toString()
        }
    }
}