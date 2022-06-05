package com.bugs.posisiin01.viewModel

import androidx.lifecycle.ViewModel
import com.bugs.posisiin01.model.DataSampel
import com.bugs.posisiin01.model.Kvalue
import com.bugs.posisiin01.model.Ssid
import com.bugs.posisiin01.repository.SampelRepository

class SampelViewModel(private val repository: SampelRepository) : ViewModel() {

    // DATA SAMPEL
    fun getAllSampel() = repository.getAllSampel()
    fun addSampel(dataSampel: DataSampel) = repository.addSampel(dataSampel)
    fun deleteSampel(dataSampel: DataSampel) = repository.deleteSampel(dataSampel)

    // SSID
    fun getSsid() = repository.getSsid()
    fun setSsid(ssid: Ssid) = repository.setSsid(ssid)

    // K VALUE
    fun getKvalue() = repository.getKvalue()
    fun setKvalue(kvalue: Kvalue) = repository.setKvalue(kvalue)

}