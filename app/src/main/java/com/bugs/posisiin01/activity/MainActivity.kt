package com.bugs.posisiin01.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bugs.posisiin01.activity.dialog.AutentikasiAdmin
import com.bugs.posisiin01.activity.dialog.LoginDialog
import com.bugs.posisiin01.databinding.ActivityMainBinding
import com.bugs.posisiin01.tools.Constants
import com.bugs.posisiin01.tools.DataState
import com.bugs.posisiin01.viewModel.SampelViewModel
import com.bugs.posisiin01.viewModel.SampelViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : BaseActivity(), View.OnClickListener, AutentikasiAdmin {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SampelViewModel

    private val uiScope = CoroutineScope(Dispatchers.Main)

    init {
        uiScope.launch { getSsid() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, SampelViewModelFactory())[SampelViewModel::class.java]

        binding.btnCariPosisi.setOnClickListener(this)
        binding.btnKelolaData.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id){
            binding.btnCariPosisi.id -> {
                val intent = Intent(this@MainActivity, OnlineActivity::class.java)
                startActivity(intent)
            }
            binding.btnKelolaData.id -> {
                showDialog()
            }
        }
    }

    /*
    DIALOG LOGIN ADMIN
     */
    private fun showDialog() {
        LoginDialog().show(supportFragmentManager, "Login Admin Dialog")
    }

    override fun setEmailPassword(email: String, password: String) {
        showProgressBar("Autentikasi Admin")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    hideProgressBar()
                    val intent = Intent(this@MainActivity, PengaturanActivity::class.java)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                hideProgressBar()
                //Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                showSnackBarCustom(it.localizedMessage!!, true)
            }
    }

    // GET SSID
    private suspend fun getSsid(){
        viewModel.getSsid().collect {
            when(it) {
                is DataState.Loading -> { showProgressBar("Mengunduh informasi SSID Access Point") }
                is DataState.Success -> {
                    hideProgressBar()
                    // change constants.ssid ap1 ap2 ap3 value
                    if (it.data!=null) {
                        Constants.SSID_AP1 = it.data.ap1!!
                        Constants.SSID_AP2 = it.data.ap2!!
                        Constants.SSID_AP3 = it.data.ap3!!

                        // trigger k value function
                        getKvalue()
                    } else {
                        showSnackBarCustom("Data SSID kosong", true)
                    }
                }
                is DataState.Failed -> {
                    hideProgressBar()
                    showSnackBarCustom(it.info, true)
                }
            }
        }
    }

    // GET K VALUE
    private suspend fun getKvalue(){
        viewModel.getKvalue().collect {
            when(it) {
                is DataState.Loading -> { showProgressBar("Mengunduh informasi nilai variabel K") }
                is DataState.Success -> {
                    hideProgressBar()
                    if (it.data!=null) {
                        Constants.K_VALUE = it.data.kvalue!!
                        showSnackBarCustom("Persiapan sistem selesai :)", false)
                    } else {
                        showSnackBarCustom("Data variabel k kosong", true)
                    }
                }
                is DataState.Failed -> {
                    hideProgressBar()
                    showSnackBarCustom(it.info, true)
                }
            }
        }
    }
}