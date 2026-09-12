package com.example.tplus_jffz.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsManager {

    const val REQUEST_CAMERA = 1001
    const val REQUEST_NETWORK_STATE = 1002

    val CAMERA_PERMISSION = Manifest.permission.CAMERA
    val NETWORK_PERMISSION = Manifest.permission.ACCESS_NETWORK_STATE

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun hasNetworkPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, NETWORK_PERMISSION) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(CAMERA_PERMISSION),
            REQUEST_CAMERA
        )
    }

    fun requestNetworkPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(NETWORK_PERMISSION),
            REQUEST_NETWORK_STATE
        )
    }

    fun requestAllPermissions(activity: Activity) {
        val permissions = mutableListOf<String>()
        if (!hasCameraPermission(activity)) permissions.add(CAMERA_PERMISSION)
        if (!hasNetworkPermission(activity)) permissions.add(NETWORK_PERMISSION)
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissions.toTypedArray(),
                REQUEST_CAMERA
            )
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CAMERA || requestCode == REQUEST_NETWORK_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}
