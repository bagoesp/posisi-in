package com.bugs.posisiin01.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bugs.posisiin01.R
import com.bugs.posisiin01.activity.dialog.SetTujuan
import com.bugs.posisiin01.activity.dialog.TujuanDialog
import com.bugs.posisiin01.algorithm.Astar
import com.bugs.posisiin01.algorithm.Knn
import com.bugs.posisiin01.databinding.ActivityOnlineBinding
import com.bugs.posisiin01.model.*
import com.bugs.posisiin01.tools.Constants
import com.bugs.posisiin01.tools.DataState
import com.bugs.posisiin01.viewModel.SampelViewModel
import com.bugs.posisiin01.viewModel.SampelViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OnlineActivity : BaseActivity(), SetTujuan, View.OnClickListener {
    // binding
    private lateinit var binding: ActivityOnlineBinding
    // view model
    private lateinit var viewModel: SampelViewModel
    // coroutine
    private val uiScope = CoroutineScope(Dispatchers.Main)

    // wifi utility
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiReceiver: BroadcastReceiver

    // DATA POSITIONING
    private var dataMasukan: DataMasukan? = null
    private var sampelDB: List<DataSampel>? = null

    // <----------->
    // map section
    // <----------->

    // map view
    private lateinit var mCanvas: Canvas
    private val mPaint = Paint()
    private lateinit var mBitmap: Bitmap

    // sel
    private lateinit var selSel: Array<Array<Sel>>
    private var selSize = 0F
    private var hMargin = 0F
    private var vMargin = 0F

    // sel awal & tujuan
    private var lokasiAwal: Location? = null
    private var lokasiTujuan: Location? = null
    private var selAwal: Sel? = null
    private var selTujuan: Sel? = null

    // ---------------
    // companion object
    companion object {
        private const val COLS = 7
        private const val ROWS = 20
        private const val INSET = 3F
        private const val PATH_STROKE = 8F
        private const val INSET_MYLOC = 4F
        private const val INSET_ICON = 6F
    }

    // init
    init {
        createMap()
        uiScope.launch { getAllSampel() }
    }

    // bitmap
    // location
    private lateinit var iconLocation: Bitmap
    // wall
    private lateinit var wallEdge: Bitmap
    // tangga
    private lateinit var tangga: Bitmap
    // ruang off & on
    private lateinit var ruangKuliahOff: Bitmap
    private lateinit var ruangKuliahOn: Bitmap
    private lateinit var ruangToiletOff: Bitmap
    private lateinit var ruangToiletOn: Bitmap
    private lateinit var ruangAulaOff: Bitmap
    private lateinit var ruangAulaOn: Bitmap
    // icon off & on
    private lateinit var iconR306On: Bitmap
    private lateinit var iconR306Off: Bitmap
    private lateinit var iconR307On: Bitmap
    private lateinit var iconR307Off: Bitmap
    private lateinit var iconToiletOn: Bitmap
    private lateinit var iconToiletOff: Bitmap
    private lateinit var iconR302On: Bitmap
    private lateinit var iconR302Off: Bitmap
    private lateinit var iconR303On: Bitmap
    private lateinit var iconR303Off: Bitmap
    private lateinit var iconR304On: Bitmap
    private lateinit var iconR304Off: Bitmap
    private lateinit var iconR305On: Bitmap
    private lateinit var iconR305Off: Bitmap
    private lateinit var iconLobiOn: Bitmap
    private lateinit var iconLobiOff: Bitmap
    private lateinit var iconAulaOn: Bitmap
    private lateinit var iconAulaOff: Bitmap
    private lateinit var iconManOn: Bitmap
    private lateinit var iconManOff: Bitmap
    private lateinit var iconWomanOn: Bitmap
    private lateinit var iconWomanOff: Bitmap
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, SampelViewModelFactory())[SampelViewModel::class.java]

        initBitmap()
        binding.ivMap.post {
            // show map when first activity showed
            showMap()
            showIcon()
        }

        binding.btnNavigation.isEnabled = false

        binding.btnMyLocation.setOnClickListener(this)
        binding.btnNavigation.setOnClickListener(this)
        binding.btnInfo.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onClick(v: View) {
        when(v.id) {
            binding.btnMyLocation.id -> {
                scanWifi()
            }
            binding.btnNavigation.id -> {
                showDialog()
            }
            binding.btnInfo.id -> {

            }
        }
    }

    /*
        GET DATA SAMPEL
     */
    private suspend fun getAllSampel() {
        viewModel.getAllSampel().collect {
            when(it) {
                is DataState.Loading -> { showProgressBar("Mengunduh data sampel terbaru") }
                is DataState.Success -> {
                    hideProgressBar()
                    sampelDB = it.data
                    showSnackBarCustom("Database data sampel wi-fi terupdate", false)
                }
                is DataState.Failed -> {
                    hideProgressBar()
                    showSnackBarCustom(it.info, true)
                    binding.btnMyLocation.isEnabled = false
                }
            }
        }
    }

    /* WIFI SCAN */
    // ON START & ON STOP LIFECYCLE
    override fun onStart() {
        super.onStart()
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == intent!!.action){
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiReceiver, intentFilter)
    }
    override fun onStop() {
        unregisterReceiver(wifiReceiver)
        super.onStop()
    }

    // SUCCESS & FAILURE SCANNING
    private fun scanSuccess() {
        Log.d(Constants.LOG_TAG, "scanSuccess()")
        val results = wifiManager.scanResults
        Log.d(Constants.LOG_TAG, "scanSuccess() results = $results")
        var ap1Level : Int? = null
        var ap2Level : Int? = null
        var ap3Level : Int? = null
        for (wifi in results) {
            when(wifi.SSID) {
                Constants.SSID_AP1 -> { ap1Level = wifi.level }
                Constants.SSID_AP2 -> { ap2Level = wifi.level }
                Constants.SSID_AP3 -> { ap3Level = wifi.level }
            }
        }
        if (ap1Level!=null && ap2Level!=null && ap3Level!=null) {
            Log.d(Constants.LOG_TAG, "scanSuccess() ap1Level = $ap1Level")
            Log.d(Constants.LOG_TAG, "scanSuccess() ap2Level = $ap2Level")
            Log.d(Constants.LOG_TAG, "scanSuccess() ap3Level = $ap3Level")

            dataMasukan = DataMasukan(ap1Level, ap2Level, ap3Level)

            Log.d(Constants.LOG_TAG, "scanSuccess() Knn() dataMasukan = $dataMasukan")
            lokasiAwal = Knn(dataMasukan!!, sampelDB!!, Constants.K_VALUE).cariLokasi()
            selAwal = initSelAwal(lokasiAwal!!)
            binding.tvAwalValue.text = initLokasi(lokasiAwal!!)
            binding.btnNavigation.isEnabled = true
            if (selAwal == selTujuan) {
                showMap()
                showIcon()
                showAwal()
                showSnackBarCustom("Anda telah sampai di lokasi tujuan", false)
            } else {
                if (selTujuan!=null){
                    showTujuan()
                } else {
                    showMap()
                    showIcon()
                    showAwal()
                }
            }
            results.clear()
            Log.d(Constants.LOG_TAG, "scanSuccess() clear() results = $results")
        }
        else
            showSnackBarCustom("Data RSS tidak lengkap", true)
    }

    // SCAN FAILURE
    private fun scanFailure() {
        showSnackBarCustom("Wifi scanning failure", true)
    }

    private fun initSelAwal(lokasi: Location) : Sel? {
        return when(lokasi) {
            Location.R306 -> selSel[1][1]
            Location.R307 -> selSel[1][4]
            Location.LOB1 -> selSel[1][7]
            Location.LOB2 -> selSel[1][10]
            Location.LOB3 -> selSel[1][14]
            Location.AULA -> selSel[3][17]
            Location.TLPI -> selSel[5][0]
            Location.TLPA -> selSel[5][2]
            Location.R302 -> selSel[5][4]
            Location.R303 -> selSel[5][7]
            Location.R304 -> selSel[5][10]
            Location.R305 -> selSel[5][13]
            Location.KRD1 -> selSel[3][1]
            Location.KRD2 -> selSel[3][4]
            Location.KRD3 -> selSel[3][7]
            Location.KRD4 -> selSel[3][10]
            Location.KRD5 -> selSel[3][13]
            else -> null
        }
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
                    showToast("Permission not granted")
            }
        }
    }


    /*
    DIALOG LOKASI TUJUAN
     */
    private fun showDialog() {
        TujuanDialog().show(supportFragmentManager,"Lokasi Tujuan Dialog")
    }

    /*
    MAP VIEW
     */
    private fun showIcon() {
        // r306
        if(lokasiTujuan == Location.R306)
            mCanvas.drawBitmap(iconR306On, null, RectF(selSize,selSize/2+ INSET_ICON, 2*selSize, 2*selSize - (selSize/4) + INSET_ICON),mPaint)
        else
            mCanvas.drawBitmap(iconR306Off, null, RectF(selSize,selSize/2 + INSET_ICON, 2*selSize, 2*selSize - (selSize/4) + INSET_ICON),mPaint)
        // r307
        if (lokasiTujuan == Location.R307)
            mCanvas.drawBitmap(iconR307On, null, RectF(selSize,4*selSize - (selSize/2) + INSET_ICON, 2*selSize, 5*selSize - (selSize/4) + INSET_ICON),mPaint)
        else
            mCanvas.drawBitmap(iconR307Off, null, RectF(selSize,4*selSize - (selSize/2) + INSET_ICON, 2*selSize, 5*selSize - (selSize/4) + INSET_ICON),mPaint)
        // lobi
        if (lokasiTujuan == Location.LOB2)
            mCanvas.drawBitmap(iconLobiOn, null, RectF(selSize,10*selSize - (selSize/2) + INSET_ICON, 2*selSize, 11*selSize - (selSize/4) + INSET_ICON),mPaint)
        else
            mCanvas.drawBitmap(iconLobiOff, null, RectF(selSize,10*selSize - (selSize/2) + INSET_ICON, 2*selSize, 11*selSize - (selSize/4) + INSET_ICON),mPaint)
        // r302
        if (lokasiTujuan == Location.R302)
            mCanvas.drawBitmap(iconR302On, null, RectF(5*selSize,4*selSize - (selSize/2) + INSET_ICON, 6*selSize, 5*selSize - (selSize/4) + INSET_ICON),mPaint)
        else
            mCanvas.drawBitmap(iconR302Off, null, RectF(5*selSize,4*selSize - (selSize/2) + INSET_ICON, 6*selSize, 5*selSize - (selSize/4) + INSET_ICON),mPaint)
        // r303
        if (lokasiTujuan == Location.R303)
            mCanvas.drawBitmap(iconR303On, null, RectF(5*selSize,7*selSize - (selSize/2) + INSET_ICON, 6*selSize, 8*selSize - (selSize/4) + INSET_ICON),mPaint)
        else
            mCanvas.drawBitmap(iconR303Off, null, RectF(5*selSize,7*selSize - (selSize/2) + INSET_ICON, 6*selSize, 8*selSize - (selSize/4) + INSET_ICON),mPaint)
        // r304
        if (lokasiTujuan == Location.R304)
            mCanvas.drawBitmap(iconR304On, null, RectF(5*selSize,10*selSize - (selSize/2) + INSET_ICON, 6*selSize, 11*selSize - (selSize/4) + INSET_ICON),mPaint)
        else
            mCanvas.drawBitmap(iconR304Off, null, RectF(5*selSize,10*selSize - (selSize/2) + INSET_ICON, 6*selSize, 11*selSize - (selSize/4) + INSET_ICON),mPaint)
        // r305
        if (lokasiTujuan == Location.R305)
            mCanvas.drawBitmap(iconR305On, null, RectF(5*selSize,13*selSize - (selSize/2) + INSET_ICON, 6*selSize, 14*selSize - (selSize/4) + INSET_ICON),mPaint)
        else
            mCanvas.drawBitmap(iconR305Off, null, RectF(5*selSize,13*selSize - (selSize/2) + INSET_ICON, 6*selSize, 14*selSize - (selSize/4) + INSET_ICON),mPaint)
        // aula
        if (lokasiTujuan == Location.AULA)
            mCanvas.drawBitmap(iconAulaOn, null, RectF(3*selSize,17*selSize - (selSize/2) + INSET_ICON, 4*selSize, 18*selSize - (selSize/4) + INSET_ICON),mPaint)
        else
            mCanvas.drawBitmap(iconAulaOff, null, RectF(3*selSize,17*selSize - (selSize/2) + INSET_ICON, 4*selSize, 18*selSize - (selSize/4) + INSET_ICON),mPaint)
        // toilet pi
        if (lokasiTujuan == Location.TLPI)
            mCanvas.drawBitmap(iconWomanOn, null, RectF(5*selSize + (selSize/6) +2F,selSize/2.5F+3F+3F, 6*selSize - (selSize/6)-2F, selSize + (selSize/2) - (selSize/2.5F)-3F+3F),mPaint)
        else
            mCanvas.drawBitmap(iconWomanOff, null, RectF(5*selSize + (selSize/6)+2F,selSize/2.5F+3F+3F, 6*selSize - (selSize/6)-2F, selSize + (selSize/2) - (selSize/2.5F)-3F+3F),mPaint)
        // toilet pa
        if (lokasiTujuan == Location.TLPA)
            mCanvas.drawBitmap(iconManOn, null, RectF(5*selSize + (selSize/6)+2F, selSize + (selSize/2) + (selSize/2.5F)+3F+3F, 6*selSize - (selSize/6)-2F, 3*selSize - (selSize/2.5F)-3F+3F),mPaint)
        else
            mCanvas.drawBitmap(iconManOff, null, RectF(5*selSize + (selSize/6+2F), selSize + (selSize/2) + (selSize/2.5F)+3F+3F, 6*selSize - (selSize/6)-2F, 3*selSize - (selSize/2.5F)-3F+3F),mPaint)
    }
    private fun initBitmap() {
        iconLocation = BitmapFactory.decodeResource(resources, R.drawable.icon_location_new)
        wallEdge = BitmapFactory.decodeResource(resources, R.drawable.wall_icon)
        ruangKuliahOff = BitmapFactory.decodeResource(resources, R.drawable.ruang_off_icon)
        ruangKuliahOn = BitmapFactory.decodeResource(resources, R.drawable.ruang_on_icon)
        ruangAulaOff = BitmapFactory.decodeResource(resources, R.drawable.ruang_aula_off_icon)
        ruangAulaOn = BitmapFactory.decodeResource(resources, R.drawable.ruang_aula_on_icon)
        ruangToiletOff = BitmapFactory.decodeResource(resources, R.drawable.ruang_toilet_off_icon)
        ruangToiletOn = BitmapFactory.decodeResource(resources, R.drawable.ruang_toilet_on_icon)
        tangga = BitmapFactory.decodeResource(resources, R.drawable.tangga_icon)
        iconToiletOff = BitmapFactory.decodeResource(resources, R.drawable.icon_toilet_off)
        iconToiletOn = BitmapFactory.decodeResource(resources, R.drawable.icon_toilet_on)
        iconR302Off = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_302_off)
        iconR302On = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_302_on)
        iconR303Off = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_303_off)
        iconR303On = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_303_on)
        iconR304Off = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_304_off)
        iconR304On = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_304_on)
        iconR305Off = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_305_off)
        iconR305On = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_305_on)
        iconR306Off = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_306_off)
        iconR306On = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_306_on)
        iconR307Off = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_307_off)
        iconR307On = BitmapFactory.decodeResource(resources, R.drawable.icon_kuliah_307_on)
        iconLobiOff = BitmapFactory.decodeResource(resources, R.drawable.icon_lobi_off)
        iconLobiOn = BitmapFactory.decodeResource(resources, R.drawable.icon_lobi_on)
        iconAulaOff = BitmapFactory.decodeResource(resources, R.drawable.icon_aula_off)
        iconAulaOn = BitmapFactory.decodeResource(resources, R.drawable.icon_aula_on)
        iconWomanOn = BitmapFactory.decodeResource(resources, R.drawable.icon_woman_on)
        iconWomanOff = BitmapFactory.decodeResource(resources, R.drawable.icon_woman_off)
        iconManOn = BitmapFactory.decodeResource(resources, R.drawable.icon_man_on)
        iconManOff= BitmapFactory.decodeResource(resources, R.drawable.icon_man_off)
    }
    private fun showMap(){
        mBitmap = Bitmap.createBitmap(binding.ivMap.width, binding.ivMap.height, Bitmap.Config.ARGB_8888)
        binding.ivMap.setImageBitmap(mBitmap)
        mCanvas = Canvas(mBitmap)
        mCanvas.save()

        selSize = ( binding.ivMap.width / (COLS+5) ).toFloat()
        hMargin = (binding.ivMap.width - (selSize*COLS)) / 2
        vMargin = (binding.ivMap.height - (selSize* ROWS)) / 2

        // wall
        mCanvas.translate(hMargin-7F, vMargin-7F)
        mCanvas.drawBitmap(wallEdge, null, RectF(0F, 0F, COLS*selSize + 14, ROWS*selSize + 14), mPaint)
        mCanvas.restore()

        mCanvas.translate(hMargin, vMargin)
        mCanvas.save()
        // RUANG
        // r306
        if (lokasiTujuan == Location.R306)
            mCanvas.drawBitmap(ruangKuliahOn, null, RectF(INSET, INSET, selSize*3-INSET, selSize*3- INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangKuliahOff, null, RectF(INSET, INSET, selSize*3-INSET, selSize*3- INSET), mPaint)
        // r307
        if (lokasiTujuan == Location.R307)
            mCanvas.drawBitmap(ruangKuliahOn, null, RectF(INSET, 3*selSize+INSET, selSize*3-INSET, selSize*6- INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangKuliahOff, null, RectF(INSET, 3*selSize+INSET, selSize*3-INSET, selSize*6- INSET), mPaint)
        // toilet pi
        if (lokasiTujuan == Location.TLPI)
            mCanvas.drawBitmap(ruangToiletOn, null, RectF(4*selSize+INSET, INSET, selSize*7-INSET, (selSize/2) + selSize - INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangToiletOff, null, RectF(4*selSize+INSET, INSET, selSize*7-INSET, (selSize/2) + selSize - INSET), mPaint)
        // toilet pa
        if (lokasiTujuan == Location.TLPA)
            mCanvas.drawBitmap(ruangToiletOn, null, RectF(4*selSize+INSET, (selSize/2) + selSize+INSET, selSize*7-INSET, (3*selSize) - INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangToiletOff, null, RectF(4*selSize+INSET, (selSize/2) + selSize+INSET, selSize*7-INSET, (3*selSize) - INSET), mPaint)
        // r302
        if (lokasiTujuan == Location.R302)
            mCanvas.drawBitmap(ruangKuliahOn, null, RectF(4*selSize+INSET, 3*selSize+INSET, selSize*7-INSET, 6*selSize - INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangKuliahOff, null, RectF(4*selSize+INSET, 3*selSize+INSET, selSize*7-INSET, 6*selSize - INSET), mPaint)
        // r303
        if (lokasiTujuan == Location.R303)
            mCanvas.drawBitmap(ruangKuliahOn, null, RectF(4*selSize+INSET, 6*selSize+INSET, selSize*7-INSET, 9*selSize - INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangKuliahOff, null, RectF(4*selSize+INSET, 6*selSize+INSET, selSize*7-INSET, 9*selSize - INSET), mPaint)
        // r304
        if (lokasiTujuan == Location.R304)
            mCanvas.drawBitmap(ruangKuliahOn, null, RectF(4*selSize+INSET, 9*selSize+INSET, selSize*7-INSET, 12*selSize - INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangKuliahOff, null, RectF(4*selSize+INSET, 9*selSize+INSET, selSize*7-INSET, 12*selSize - INSET), mPaint)
        // r305
        if (lokasiTujuan == Location.R305)
            mCanvas.drawBitmap(ruangKuliahOn, null, RectF(4*selSize+INSET, 12*selSize+INSET, selSize*7-INSET, 15*selSize - INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangKuliahOff, null, RectF(4*selSize+INSET, 12*selSize+INSET, selSize*7-INSET, 15*selSize - INSET), mPaint)
        // aula
        if (lokasiTujuan == Location.AULA)
            mCanvas.drawBitmap(ruangAulaOn, null, RectF(INSET, 15*selSize+INSET, selSize*7-INSET, 20*selSize - INSET), mPaint)
        else
            mCanvas.drawBitmap(ruangAulaOff, null, RectF(INSET, 15*selSize+INSET, selSize*7-INSET, 20*selSize - INSET), mPaint)
        // tangga atas
        mCanvas.drawBitmap(tangga, null, RectF(INSET, 8*selSize+INSET, 3*selSize- INSET, 9*selSize-INSET), mPaint)
        // tangga bawah
        mCanvas.drawBitmap(tangga, null, RectF(INSET, 12*selSize+ INSET, 3*selSize- INSET, 13*selSize- INSET), mPaint)
    }

    private fun createMap(){
        selSel = Array(COLS) { Array(ROWS) { Sel() } }
        for (x in 0 until COLS) {
            for(y in 0 until ROWS) {
                selSel[x][y].x = x
                selSel[x][y].y = y
            }
        }

        // edge wall
        // left wall
        for (y in 0 until ROWS) {
            val x = 0
            selSel[x][y].left = true
        }
        // top wall
        for (x in 0 until COLS){
            val y = 0
            selSel[x][y].top = true
        }
        // right wall
        for (y in 0 until ROWS) {
            val x = COLS-1
            selSel[x][y].right = true
        }
        // bottom wall
        for (x in 0 until COLS){
            val y = ROWS-1
            selSel[x][y].bottom = true
        }

        // ruang 306
        // right wall
        selSel[2][0].right = true
        selSel[2][2].right = true
        //bottom wall
        selSel[0][2].bottom = true
        selSel[1][2].bottom = true
        selSel[2][2].bottom = true

        // koridor 1
        //left wall
        selSel[3][0].left = true
        selSel[3][2].left = true
        //right wall
        selSel[3][1].right = true

        // toilet pi
        // bottom wall
        selSel[4][0].bottom = true
        selSel[5][0].bottom = true
        selSel[6][0].bottom = true

        // toilet pa
        // top wall
        selSel[4][1].top = true
        selSel[5][1].top = true
        selSel[6][1].top = true
        //bottom wall
        selSel[4][2].bottom = true
        selSel[5][2].bottom = true
        selSel[6][2].bottom = true

        // ruang 307
        // top wall
        selSel[0][3].top = true
        selSel[1][3].top = true
        selSel[2][3].top = true
        //right wall
        selSel[2][3].right = true
        selSel[2][5].right = true
        //bottom wall
        selSel[0][5].bottom = true
        selSel[1][5].bottom = true
        selSel[2][5].bottom = true

        // koridor 2
        //left wall
        selSel[3][3].left = true
        selSel[3][5].left = true
        //right wall
        selSel[3][3].right = true
        selSel[3][5].right = true

        // ruang 302
        //topwall
        selSel[4][3].top = true
        selSel[5][3].top = true
        selSel[6][3].top = true
        //leftwall
        selSel[4][3].left = true
        selSel[4][5].left = true
        //bottom wall
        selSel[4][5].bottom = true
        selSel[5][5].bottom = true
        selSel[6][5].bottom = true

        // lobi 1
        // top wall
        selSel[0][6].top = true
        selSel[1][6].top = true
        selSel[2][6].top = true
        // bottom wall
        selSel[0][7].bottom = true
        selSel[1][7].bottom = true
        selSel[2][7].bottom = true

        // koridor 2
        // left wall
        selSel[3][8].left = true
        // rigt wall
        selSel[3][6].right = true
        selSel[3][8].right = true

        // ruang 303
        // top wall
        selSel[4][6].top = true
        selSel[5][6].top = true
        selSel[6][6].top = true
        //left wall
        selSel[4][6].left = true
        selSel[4][8].left = true
        //bottom wall
        selSel[4][8].bottom = true
        selSel[5][8].bottom = true
        selSel[6][8].bottom = true

        // lobi 2
        // top wall
        selSel[0][9].top = true
        selSel[1][9].top = true
        selSel[2][9].top = true
        // bottom wall
        selSel[0][11].bottom = true
        selSel[1][11].bottom = true
        selSel[2][11].bottom = true

        //koridor 3
        //right wall
        selSel[3][9].right = true
        selSel[3][11].right = true

        // ruang 304
        // top wall
        selSel[4][9].top = true
        selSel[5][9].top = true
        selSel[6][9].top = true
        // left wall
        selSel[4][9].left = true
        selSel[4][11].left = true
        // bottom wall
        selSel[4][11].bottom = true
        selSel[5][11].bottom = true
        selSel[6][11].bottom = true

        // lobi 3
        //top wall
        selSel[0][13].top = true
        selSel[1][13].top = true
        selSel[2][13].top = true
        // bottom wall
        selSel[0][14].bottom = true
        selSel[1][14].bottom = true
        selSel[2][14].bottom = true

        // koridor 4
        // left wall
        selSel[3][12].left = true
        // right wall
        selSel[3][12].right = true
        selSel[3][14].right = true

        //ruang 305
        // top wall
        selSel[4][12].top = true
        selSel[5][12].top = true
        selSel[6][12].top = true
        // left wall
        selSel[4][12].left = true
        selSel[4][14].left = true
        //bottom wall
        selSel[4][14].bottom = true
        selSel[5][14].bottom = true
        selSel[6][14].bottom = true

        // aula
        // top wall
        selSel[0][15].top = true
        selSel[1][15].top = true
        selSel[2][15].top = true
        selSel[4][15].top = true
        selSel[5][15].top = true
        selSel[6][15].top = true
    }


    /*
    INFORMASI NAVIGASI
     */
    // set selTujuan coordinate based on lokasiTujuan value
    private fun initSelTujuan(lokasi: Location) {
        selTujuan = when(lokasi) {
            Location.R306 -> {
                selSel[1][1]
            }
            Location.R307 -> {
                selSel[1][4]
            }
            Location.LOB2 -> {
                selSel[1][10]
            }
            Location.TLPI -> {
                selSel[5][0]
            }
            Location.TLPA -> {
                selSel[5][2]
            }
            Location.R302 -> {
                selSel[5][4]
            }
            Location.R303 -> {
                selSel[5][7]
            }
            Location.R304 -> {
                selSel[5][10]
            }
            Location.R305 -> {
                selSel[5][13]
            }
            Location.AULA -> {
                selSel[3][17]
            }
            else -> {
                null
            }
        }
    }

    // interface function to sel sel and string value of tujuan sel
    override fun setTujuanValue(lokasi: Location?) {
        if (lokasi!=null) {
            if (lokasi == lokasiAwal) {
                showSnackBarCustom("Posisi awal dan lokasi tujuan sama", true)
            } else {
                lokasiTujuan = lokasi
                showTujuan()
            }
        }
    }

    private fun showTujuan(){
        binding.tvTujuanValue.text = initLokasi(lokasiTujuan!!)
        initSelTujuan(lokasiTujuan!!)
        showMap()
        showRute(selAwal!!, selTujuan!!)
        showIcon()
        showAwal()
        showSnackBarCustom("Rute ditemukan", false)
    }

    /*
    RUTE NAVIGASI
     */
    private fun showRute(awal: Sel, tujuan: Sel){
        val rute = Astar(selSel, awal, tujuan).cariRute()
        if (rute.isNotEmpty()) {
            mPaint.color = resources.getColor(R.color.ireng)
            mPaint.strokeWidth = PATH_STROKE
            for (i in 0 until rute.size) {
                if (i == 0) {
                    val current = rute[i]
                    val next = rute[i+1]
                    mCanvas.save()
                    mCanvas.translate(current.x * selSize, current.y * selSize)
                    drawDir(checkDir(next, current))
                    binding.ivMap.invalidate()
                    mCanvas.restore()
                }
                else if (i>0 && i<rute.size-1) {
                    val previous = rute[i-1]
                    val current = rute[i]
                    val next = rute[i+1]
                    mCanvas.save()
                    mCanvas.translate(current.x * selSize, current.y * selSize)

                    // draw previous
                    drawDir(checkDir(previous, current))
                    // draw next
                    drawDir(checkDir(next, current))
                    binding.ivMap.invalidate()
                    mCanvas.restore()
                }
                else {
                    val previous = rute[i-1]
                    val current = rute[i]
                    mCanvas.save()
                    mCanvas.translate(current.x * selSize, current.y * selSize)
                    drawDir(checkDir(previous, current))
                    binding.ivMap.invalidate()
                    mCanvas.restore()
                }
            }
        }
    }

    private fun drawDir(direction: Direction) {
        when(direction) {
            Direction.B -> {
                mCanvas.drawLine(0F, selSize/2, selSize/2+3.5F, selSize/2, mPaint)
            }
            Direction.BL -> {
                mCanvas.drawLine(0F, 0F, selSize/2+2F, selSize/2+2F, mPaint)
            }
            Direction.U -> {
                mCanvas.drawLine(selSize/2, 0F, selSize/2, selSize/2+3.5F, mPaint)
            }
            Direction.TL -> {
                mCanvas.drawLine(selSize, 0F, selSize/2-2F, selSize/2+2F, mPaint)
            }
            Direction.T -> {
                mCanvas.drawLine(selSize, selSize/2, selSize/2-3.5F, selSize/2, mPaint)
            }
            Direction.TG -> {
                mCanvas.drawLine(selSize, selSize, selSize/2-2F, selSize/2-2F, mPaint)
            }
            Direction.S -> {
                mCanvas.drawLine(selSize/2, selSize/2-3.5F, selSize/2, selSize, mPaint)
            }
            Direction.BD -> {
                mCanvas.drawLine(0F, selSize, selSize/2+2F, selSize/2-2F, mPaint)
            }
        }
    }

    private fun checkDir(from: Sel, next: Sel) : Direction{
        // barat
        if (from.x < next.x && from.y == next.y)
            return Direction.B
        // barat laut
        if (from.x < next.x && from.y < next.y)
            return Direction.BL
        // utara
        if (from.x == next.x && from.y < next.y)
            return Direction.U
        // timur laut
        if (from.x > next.x && from.y < next.y)
            return Direction.TL
        // timur
        if (from.x > next.x && from.y == next.y)
            return Direction.T
        // tenggara
        if (from.x > next.x && from.y > next.y)
            return Direction.TG
        // selatan
        if (from.x == next.x && from.y > next.y)
            return Direction.S
        // barat daya
        if (from.x < next.x && from.y > next.y)
            return Direction.BD

        return Direction.B
    }

    /*
    SHOW AWAL & TUJUAN
     */
    private fun showAwal() {
        if (selAwal!=null){
            mCanvas.save()
            mCanvas.translate(selAwal!!.x * selSize, selAwal!!.y * selSize)
            mCanvas.drawBitmap(iconLocation, null, RectF(INSET_MYLOC, INSET_MYLOC, selSize- INSET_MYLOC, selSize- INSET_MYLOC), mPaint)
            binding.ivMap.invalidate()
            mCanvas.restore()
            showSnackBarCustom("Posisi anda ditemukan", false)
        }
    }
}