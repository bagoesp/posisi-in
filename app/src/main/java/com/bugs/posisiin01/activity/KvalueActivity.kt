package com.bugs.posisiin01.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bugs.posisiin01.databinding.ActivityKvalueBinding
import com.bugs.posisiin01.model.Kvalue
import com.bugs.posisiin01.tools.Constants
import com.bugs.posisiin01.tools.DataState
import com.bugs.posisiin01.viewModel.SampelViewModel
import com.bugs.posisiin01.viewModel.SampelViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class KvalueActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityKvalueBinding
    private lateinit var viewModel: SampelViewModel

    private val uiScope = CoroutineScope(Dispatchers.Main)

    init {
        uiScope.launch { getKvalue() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKvalueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, SampelViewModelFactory())[SampelViewModel::class.java]

        binding.btnUpKvalue.setOnClickListener(this)
        binding.btnDownKvalue.setOnClickListener(this)
        binding.btnBackKvalue.setOnClickListener(this)
        binding.btnSimpanKvalue.setOnClickListener(this)
        binding.btnBatalKvalue.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            binding.btnUpKvalue.id -> {
                upValue()
            }
            binding.btnDownKvalue.id -> {
                downValue()
            }
            binding.btnSimpanKvalue.id -> {
                uiScope.launch {
                    setKvalue(getFormKvalue())
                }
            }
            binding.btnBatalKvalue.id -> {
                resetKvalue()
            }
            binding.btnBackKvalue.id -> {}
        }
    }

    private fun upValue() {
        val temp = binding.etKvalue.text.toString().toInt()
        val newVal = temp+1
        binding.etKvalue.setText(newVal.toString())
        if (binding.etKvalue.text.toString().toInt() > 1) {
            binding.btnDownKvalue.isEnabled = true
        }
    }

    private fun downValue() {
        val temp = binding.etKvalue.text.toString().toInt()
        if (temp > 1) {
            val newVal = temp - 1
            binding.etKvalue.setText(newVal.toString())
        } else {
            binding.btnDownKvalue.isEnabled = false
        }
    }

    private suspend fun getKvalue(){
        viewModel.getKvalue().collect {
            when(it) {
                is DataState.Loading -> { showProgressBar("Mendapatkan informasi variabel K") }
                is DataState.Success -> {
                    hideProgressBar()
                    if (it.data!=null) {
                        Constants.K_VALUE = it.data.kvalue!!
                    }
                    setKvalue()
                }
                is DataState.Failed -> {
                    showSnackBarCustom(it.info, true)
                }
            }
        }
    }

    private suspend fun setKvalue(kvalue: Kvalue){
        viewModel.setKvalue(kvalue).collect {
            when(it) {
                is DataState.Loading -> { showProgressBar("Menyimpan informasi variabel K") }
                is DataState.Success -> {
                    hideProgressBar()
                    showSnackBarCustom("Data variabel k berhasil disimpan", false)
                    getKvalue()
                }
                is DataState.Failed -> {
                    showToast(it.info)
                }
            }
        }
    }

    private fun setKvalue() {
        binding.etKvalue.setText(Constants.K_VALUE.toString())
    }
    private fun resetKvalue() {
        binding.etKvalue.setText(Constants.K_VALUE.toString())
        showSnackBarCustom("Perubahan dibatalkan", true)
    }

    private fun getFormKvalue(): Kvalue{
        val k = binding.etKvalue.text.toString().toInt()
        return Kvalue(k)
    }

}