package com.example.tugasakhir.location

import android.graphics.Canvas
import android.widget.TextView
import com.example.tugasakhir.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindow(
    layoutResId: Int,
    mapView: MapView,
    title: String,
    private val marker: Marker
) : InfoWindow(layoutResId, mapView) {

    private val titleTextView: TextView = mView.findViewById(R.id.title)

    init {
        titleTextView.text = title
    }

    override fun onOpen(item: Any?) {
        // Override this method if needed
    }

    override fun onClose() {
        // Override this method if needed
    }
}
