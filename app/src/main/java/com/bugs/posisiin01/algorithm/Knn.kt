package com.bugs.posisiin01.algorithm

import android.util.Log
import com.bugs.posisiin01.model.DataMasukan
import com.bugs.posisiin01.model.DataSampel
import com.bugs.posisiin01.model.DataSampelKnn
import com.bugs.posisiin01.model.Location
import com.bugs.posisiin01.tools.Constants

class Knn(
    private val dataMasukan: DataMasukan,
    private val sampelDB: List<DataSampel>,
    private val kValue: Int
) {
    // RETURN LOCATION
    fun cariLokasi() : Location {
        val listSampelKnn = mutableListOf<DataSampelKnn>()
        listSampelKnn.clear()
        Log.d(Constants.LOG_TAG, "Knn() listsampelknn = $listSampelKnn")
        for (sampel in sampelDB) {
            val eucDist = calEucDist(dataMasukan, sampel)
            listSampelKnn.add(DataSampelKnn(sampel.lokasi, eucDist))
        }
        Log.d(Constants.LOG_TAG, "Knn() listsampelknn = $listSampelKnn")
        // SORTING
        val sortedByEuc = listSampelKnn.sortedBy { it.euclidean }
        Log.d(Constants.LOG_TAG, "Knn() listsampelknn = ${listSampelKnn.sortedBy { it.euclidean }}")
        val sortedByK = sortedByEuc.take(kValue)
        Log.d(Constants.LOG_TAG, "Knn() sortedWithK = $sortedByK")
        val voted = sortedByK.maxByOrNull { it.lokasi!! }!!.lokasi
        Log.d(Constants.LOG_TAG, "Knn() voted = $voted")

        return voted!!
    }

    // HITUNG JARAK EUCLIDEAN
    private fun calEucDist(dataMasukan: DataMasukan, dataSampel: DataSampel): Double {
        val ap1 = pangkat(dataMasukan.ap1!! - dataSampel.ap1!!).toDouble()
        val ap2 = pangkat(dataMasukan.ap2!! - dataSampel.ap2!!).toDouble()
        val ap3 = pangkat(dataMasukan.ap3!! - dataSampel.ap3!!).toDouble()
        return kotlin.math.sqrt(ap1 + ap2 + ap3)
    }
    // PANGKAT
    private fun pangkat(nilai: Int) : Int = nilai * nilai
}