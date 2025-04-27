package com.example.unlocked.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen() {
    // Default camera position: world view
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = true
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    )
}