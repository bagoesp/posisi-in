package com.bugs.posisiin01.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bugs.posisiin01.R
import com.bugs.posisiin01.activity.dialog.SimpanDialog
import com.bugs.posisiin01.activity.dialog.SimpanSampel
import com.bugs.posisiin01.databinding.ActivityTambahBinding
import com.bugs.posisiin01.model.DataSampel
import com.bugs.posisiin01.tools.Constants
import com.bugs.posisiin01.tools.DataState
import com.bugs.posisiin01.viewModel.SampelViewModel
import com.bugs.posisiin01.viewModel.SampelViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TambahActivity : BaseActivity(), View.OnClickListener, SimpanSampel {
    // binding
    private lateinit var binding: ActivityTambahBinding
    // view model
    private lateinit var viewModel: SampelViewModel
    // coroutine scope
    private val uiScope = CoroutineScope(Dispatchers.Main)

    // wifi utility
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this, SampelViewModelFactory())[SampelViewModel::class.java]
        val labelAdapter = ArrayAdapter(this, R.layout.ac_label_lokasi, Constants.label_items)
        binding.acLabel.setAdapter(labelAdapter)

        binding.btnBackTambah.setOnClickListener(this)
        binding.btnScanTambah.setOnClickListener(this)
        binding.btnSimpanTambah.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id) {
            binding.btnBackTambah.id -> {
                finish()
            }
            binding.btnScanTambah.id -> {
                scanWifi()
            }
            binding.btnSimpanTambah.id -> {
                if (checkForm())
                    SimpanDialog().show(supportFragmentManager, "SimpanDialogFragment")
                else
                    showSnackBarCustom("Informasi data sampel tidak lengkap", true)
            }
        }
    }

    /*
    WIFI-UTILITY
     */
    // ON START & ON STOP LIFECYCLE
    override fun onStart() {
        super.onStart()
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //val success = intent!!.getBooleanExtra(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION, false)
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == intent!!.action) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
    }
    override fun onStop() {
        unregisterReceiver(wifiScanReceiver)
        super.onStop()
    }

    // WIFI SCANNING SUCCESS & FAILURE
    private fun scanSuccess() {
        showSnackBarCustom("Scan success", false)
        val results = wifiManager.scanResults
        for (wifi in results) {
            when(wifi.SSID) {
                Constants.SSID_AP1 -> { binding.etAp1.setText(wifi.level.toString()) }
                Constants.SSID_AP2 -> { binding.etAp2.setText(wifi.level.toString()) }
                Constants.SSID_AP3 -> { binding.etAp3.setText(wifi.level.toString()) }
            }
        }
    }
    private fun scanFailure() {
        showSnackBarCustom("Scan failure", true)
    }

    // SCANNING WIFI
    private fun scanWifi() {
        if (checkState()) {
            // check permission coarse >= marshmallow
            if (Constants.DEVICE_VERSION >= Constants.M_VERSION)
                checkPermission()
            else
                doScan()
        }
    }
    private fun doScan() {
        wifiManager.startScan()
    }
    private fun checkState() : Boolean {
        return when (wifiManager.wifiState) {
            WifiManager.WIFI_STATE_DISABLED -> {
                showSnackBarCustom("Wifi tidak aktif", true)
                false
            }
            WifiManager.WIFI_STATE_ENABLED -> {
                true
            }
            else -> false
        }
    }
    private fun checkPermission() {
        val selfPermission = ContextCompat.checkSelfPermission(this, Constants.PERMISSION_COARSE)
        val granted = PackageManager.PERMISSION_GRANTED
        if (selfPermission != granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Constants.PERMISSION_COARSE,
                    Constants.PERMISSION_FINE,
                    Constants.PERMISSION_NETWORK,
                    Constants.PERMISSION_WIFI
                ),
                Constants.REQUEST_CODE
            )
        } else {
            doScan()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            Constants.REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    doScan()
                else
                    showSnackBarCustom("Wifi permission is not granted", true)
            }
        }
    }


    /*
    SIMPAN SAMPEL
     */
    // COROUTINE FUNCTION TO ADD SAMPEL
    private suspend fun addSampel(dataSampel: DataSampel) {
        viewModel.addSampel(dataSampel).collect {
            when(it) {
                is DataState.Loading -> {
                    showProgressBar("Menyimpan data sampel")
                }

                is DataState.Success -> {
                    hideProgressBar()
                    showSnackBarCustom("Data sampel berhasil disimpan", false)
                    clearForm()
                }

                is DataState.Failed -> {
                    hideProgressBar()
                    //showToast("Gagal menyimpan data sampel")
                    showSnackBarCustom("Gagal menyimpan : ${it.info}", true)
                }
            }
        }
    }
    private fun simpanData() {
        uiScope.launch {
            addSampel(ambilData())
        }
    }

    // GET DATA FROM FORM
    private fun ambilData() : DataSampel {
        val ap1 = binding.etAp1.text.toString().toInt()
        val ap2 = binding.etAp2.text.toString().toInt()
        val ap3 = binding.etAp3.text.toString().toInt()
        val lokasi = initLocation(binding.acLabel.text.toString())
        val waktu = System.currentTimeMillis()

        return DataSampel(lokasi, ap1, ap2, ap3, waktu)
    }

    private fun checkForm() : Boolean{
        return !binding.etAp1.text.isNullOrEmpty() &&
                !binding.etAp2.text.isNullOrEmpty() &&
                !binding.etAp3.text.isNullOrEmpty() &&
                !binding.acLabel.text.isNullOrEmpty()
    }
    private fun clearForm() {
        binding.etAp1.text!!.clear()
        binding.etAp2.text!!.clear()
        binding.etAp3.text!!.clear()
        binding.acLabel.text!!.clear()
    }

    override fun simpanConfirm() {
        simpanData()
    }

}