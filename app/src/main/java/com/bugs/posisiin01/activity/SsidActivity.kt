package com.bugs.posisiin01.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bugs.posisiin01.databinding.ActivitySsidBinding
import com.bugs.posisiin01.model.Ssid
import com.bugs.posisiin01.tools.Constants
import com.bugs.posisiin01.tools.DataState
import com.bugs.posisiin01.viewModel.SampelViewModel
import com.bugs.posisiin01.viewModel.SampelViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SsidActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySsidBinding
    private lateinit var viewModel: SampelViewModel

    private val uiScope = CoroutineScope(Dispatchers.Main)

    init {
        uiScope.launch { getSsid() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySsidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, SampelViewModelFactory())[SampelViewModel::class.java]

        binding.btnBackSsid.setOnClickListener(this)
        binding.btnSimpanSsid.setOnClickListener(this)
        binding.btnBatalSsid.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            binding.btnBackSsid.id -> {}
            binding.btnSimpanSsid.id -> {
                uiScope.launch {
                    setSsid(getSsidValue())
                }
            }
            binding.btnBatalSsid.id -> { resetSsidValue() }
        }
    }

    private suspend fun getSsid() {
        viewModel.getSsid().collect {
            when(it) {
                is DataState.Loading -> { showProgressBar("Mendapatkan informasi SSID") }
                is DataState.Success -> {
                    hideProgressBar()
                    if (it.data!=null) {
                        Constants.SSID_AP1 = it.data.ap1!!
                        Constants.SSID_AP2 = it.data.ap2!!
                        Constants.SSID_AP3 = it.data.ap3!!

                        setSsidValue()
                    }
                }
                is DataState.Failed -> {
                    hideProgressBar()
                    showToast(it.info)
                }
            }
        }
    }
    private suspend fun setSsid(ssid: Ssid) {
        viewModel.setSsid(ssid).collect {
            when(it) {
                is DataState.Loading -> { showProgressBar("Menyimpan informasi SSID") }
                is DataState.Success -> {
                    hideProgressBar()
                    showSnackBarCustom("Data SSID berhasil disimpan", false)
                    getSsid()
                }
                is DataState.Failed -> {
                    hideProgressBar()
                    showSnackBarCustom(it.info, true)
                }
            }
        }
    }

    private fun getSsidValue(): Ssid {
        val ssidAp1 = binding.etSsidAp1.text.toString()
        val ssidAp2 = binding.etSsidAp2.text.toString()
        val ssidAp3 = binding.etSsidAp3.text.toString()
        return Ssid(ssidAp1, ssidAp2, ssidAp3)
    }

    private fun resetSsidValue() {
        binding.etSsidAp1.setText(Constants.SSID_AP1)
        binding.etSsidAp2.setText(Constants.SSID_AP2)
        binding.etSsidAp3.setText(Constants.SSID_AP3)
        showSnackBarCustom("Perubahan dibatalkan", true)
    }

    private fun setSsidValue() {
        binding.etSsidAp1.setText(Constants.SSID_AP1)
        binding.etSsidAp2.setText(Constants.SSID_AP2)
        binding.etSsidAp3.setText(Constants.SSID_AP3)
    }
}