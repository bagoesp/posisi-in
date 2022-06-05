package com.bugs.posisiin01.activity

import android.app.Dialog
import android.graphics.Color
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bugs.posisiin01.R
import com.bugs.posisiin01.databinding.ProgressDialogBinding
import com.bugs.posisiin01.databinding.SnackbarCustomBinding
import com.bugs.posisiin01.databinding.SnackbarCustomErrorBinding
import com.bugs.posisiin01.model.Location
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    private lateinit var binding : ProgressDialogBinding
    private lateinit var pbDialog: Dialog
    private lateinit var sbBinding: SnackbarCustomBinding
    private lateinit var sbBindingError: SnackbarCustomErrorBinding

    fun showProgressBar(info: String) {
        pbDialog = Dialog(this)
        binding = ProgressDialogBinding.inflate(layoutInflater)
        pbDialog.setContentView(binding.root)
        pbDialog.setCancelable(false)
        pbDialog.setCanceledOnTouchOutside(false)

        binding.tvProgress.text = info

        pbDialog.show()
    }

    fun hideProgressBar() {
        pbDialog.dismiss()
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun initLocation(lokasi: String) : Location? {
        return when (lokasi) {
            "Ruang 306" -> Location.R306
            "Ruang 307" -> Location.R307
            "Lobi 1" -> Location.LOB1
            "Lobi 2" -> Location.LOB2
            "Lobi 3" -> Location.LOB3
            "Aula" -> Location.AULA
            "Toilet Putri" -> Location.TLPI
            "Toilet Putra" -> Location.TLPA
            "Ruang 302" -> Location.R302
            "Ruang 303" -> Location.R303
            "Ruang 304" -> Location.R304
            "Ruang 305" -> Location.R305
            "Koridor 1" -> Location.KRD1
            "Koridor 2" -> Location.KRD2
            "Koridor 3" -> Location.KRD3
            "Koridor 4" -> Location.KRD4
            "Koridor 5" -> Location.KRD5
            else -> null
        }
    }

    fun initLokasi(location: Location) : String {
        return when (location) {
            Location.KRD5 -> "Koridor 5"
            Location.KRD4 -> "Koridor 4"
            Location.KRD3 -> "Koridor 3"
            Location.KRD2 -> "Koridor 2"
            Location.KRD1 -> "Koridor 1"
            Location.R305 -> "Ruang 305"
            Location.R304 -> "Ruang 304"
            Location.R303 -> "Ruang 303"
            Location.R302 -> "Ruang 302"
            Location.TLPI -> "Toilet Putri"
            Location.AULA -> "Aula"
            Location.LOB3 -> "Lobi 3"
            Location.LOB2 -> "Lobi 2"
            Location.LOB1 -> "Lobi 1"
            Location.R307 -> "Ruang 307"
            Location.R306 -> "Ruang 306"
            Location.TLPA -> "Toilet Putra"
            else -> "-"
        }
    }

    fun showSnackBarCustom(message: String, errorMessage: Boolean) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content),"", Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackBarLayout: Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackBarLayout.setPadding(0,0,0,0)

        if (errorMessage) {
            sbBindingError = SnackbarCustomErrorBinding.inflate(layoutInflater)
            sbBindingError.tvSbValue.text = message
            snackBarLayout.addView(sbBindingError.root,0)
            snackbar.show()
        } else {
            sbBinding = SnackbarCustomBinding.inflate(layoutInflater)
            sbBinding.tvSbValue.text = message
            snackBarLayout.addView(sbBinding.root, 0)
            snackbar.show()
        }
    }

}