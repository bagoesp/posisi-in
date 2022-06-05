package com.bugs.posisiin01.tools

import android.os.Build
import com.bugs.posisiin01.model.InfoLokasi

object Constants {
    const val LOG_TAG = "pemposisian"

    const val SAMPEL_COLLECTION = "datasampel"
    const val SAMPEL_5_COLLECTION = "limasampel"
    const val SAMPEL_6_COLLECTION = "enamsampel"
    const val SAMPEL_7_COLLECTION = "tujuhsampel"
    const val SAMPEL_8_COLLECTION = "delapansampel"
    const val SAMPEL_9_COLLECTION = "sembilansampel"
    const val SAMPEL_10_COLLECTION = "sepuluhsampel"
    const val SAMPEL_DEBUG_COLLECTION = "debugsampel"
    const val POSITIONING_COLLECTION = "positioning"

    const val SSID_DOCUMENT = "ssid"
    const val KVAL_DOCUMENT = "knn"

    var K_VALUE = 3

    var SSID_AP1 = ""
    var SSID_AP2 = ""
    var SSID_AP3 = ""

    const val REQUEST_CODE = 132

    const val PERMISSION_COARSE = android.Manifest.permission.ACCESS_COARSE_LOCATION
    const val PERMISSION_FINE = android.Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_WIFI = android.Manifest.permission.ACCESS_WIFI_STATE
    const val PERMISSION_NETWORK = android.Manifest.permission.ACCESS_NETWORK_STATE

    val DEVICE_VERSION = Build.VERSION.SDK_INT
    const val M_VERSION = Build.VERSION_CODES.M

    val label_items = listOf(
        "Aula",
        "Ruang 302",
        "Ruang 303",
        "Ruang 304",
        "Ruang 305",
        "Ruang 306",
        "Ruang 307",
        "Lobi 1",
        "Lobi 2",
        "Lobi 3",
        "Koridor 1",
        "Koridor 2",
        "Koridor 3",
        "Koridor 4",
        "Koridor 5",
        "Toilet Putri",
        "Toilet Putra"
    )

    // INFO LOKASI
    val tlpi = InfoLokasi("TLPI")
    val tlpa =

}