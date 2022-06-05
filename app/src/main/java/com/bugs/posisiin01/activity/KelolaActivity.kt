package com.bugs.posisiin01.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bugs.posisiin01.R
import com.bugs.posisiin01.activity.adapter.SampelAdapter
import com.bugs.posisiin01.activity.dialog.DeleteSampel
import com.bugs.posisiin01.activity.dialog.HapusDialog
import com.bugs.posisiin01.databinding.ActivityKelolaBinding
import com.bugs.posisiin01.model.DataSampel
import com.bugs.posisiin01.tools.DataState
import com.bugs.posisiin01.viewModel.SampelViewModel
import com.bugs.posisiin01.viewModel.SampelViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class KelolaActivity : BaseActivity(), DeleteSampel{

    private lateinit var binding: ActivityKelolaBinding
    private lateinit var viewModel: SampelViewModel
    private val uiScope = CoroutineScope(Dispatchers.Main)

    lateinit var sampelSelected : DataSampel

    init {
        uiScope.launch {
            getAllSampel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, SampelViewModelFactory())[SampelViewModel::class.java]

        binding.rvKelola.layoutManager = LinearLayoutManager(this)

        binding.btnBackKelola.setOnClickListener{ finish() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    /*
    COROUTINE FUNCTION GET & DELETE
     */
    @SuppressLint("NotifyDataSetChanged")
    suspend fun getAllSampel() {
        viewModel.getAllSampel().collect {
            when(it) {
                is DataState.Loading -> {
                    showProgressBar("Meminta data")
                }

                is DataState.Success -> {
                    hideProgressBar()
                    val adapter = SampelAdapter(this, it.data)
                    binding.rvKelola.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

                is DataState.Failed -> {
                    hideProgressBar()
                    showSnackBarCustom(it.info, true)
                }
            }
        }
    }
    private suspend fun deleteSampel(dataSampel: DataSampel) {
        viewModel.deleteSampel(dataSampel).collect {
            when(it) {
                is DataState.Loading -> {
                    showProgressBar("Menghapus data sampel")
                }

                is DataState.Success -> {
                    hideProgressBar()
                    showSnackBarCustom("Data sampel berhasil dihapus", false)
                    getAllSampel()
                }

                is DataState.Failed -> {
                    hideProgressBar()
                    showSnackBarCustom(it.info, true)
                }
            }
        }
    }

    /*
    DELETE SAMPEL
     */
    override fun deleteConfirm() {
        deleteSelected()
    }
    private fun deleteSelected() {
        uiScope.launch {
            deleteSampel(sampelSelected)
        }
    }
    fun dialogHapus(dataSampel: DataSampel) {
        sampelSelected = dataSampel
        HapusDialog().show(supportFragmentManager, "dialog hapus item")
    }

}