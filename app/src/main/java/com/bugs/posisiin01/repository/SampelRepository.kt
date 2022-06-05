package com.bugs.posisiin01.repository

import com.bugs.posisiin01.model.DataSampel
import com.bugs.posisiin01.model.Kvalue
import com.bugs.posisiin01.model.Ssid
import com.bugs.posisiin01.tools.Constants
import com.bugs.posisiin01.tools.DataState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class SampelRepository {
    private val firestore = Firebase.firestore
    private val dbSampel = firestore.collection(Constants.SAMPEL_9_COLLECTION)
    private val dbPosition = firestore.collection(Constants.POSITIONING_COLLECTION)

    // DATA SAMPEL REPO
    // FLOW FUNCTION GET ALL DATA SAMPEL
    fun getAllSampel() =
        flow<DataState<List<DataSampel>>> {
        emit(DataState.loading())

        val snapshot = dbSampel
            .orderBy("waktu_input", Query.Direction.DESCENDING)
            .get()
            .await()

        val listSampel = snapshot.toObjects(DataSampel::class.java)

        emit(DataState.success(listSampel))
    }
        .catch { emit(DataState.failed(it.message!!)) }
        .flowOn(Dispatchers.IO)

    // FLOW FUNCTION ADD DATA SAMPEL
    fun addSampel(dataSampel: DataSampel)
    = flow<DataState<Boolean>> {
        emit(DataState.loading())

        val id = dataSampel.waktu_input.toString()
        val postRef = dbSampel
            .document(id)
            .set(dataSampel)
            .isSuccessful

        emit(DataState.success(postRef))
    }.catch {
        emit(DataState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    // FLOW FUNCTION DELETE DATA SAMPEL
    fun deleteSampel(dataSampel: DataSampel) =
        flow<DataState<Boolean>> {
        emit(DataState.loading())

        val id = dataSampel.waktu_input.toString()
        val delSampel = dbSampel
            .document(id)
            .delete()
            .isSuccessful

        emit(DataState.success(delSampel))
    }.catch {
        emit(DataState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    // SSID REPO
    // GET SSID VALUE
    fun getSsid() =
        flow {
            var ssid = Ssid()
            emit(DataState.loading())
            val snapshot = dbPosition
                .document(Constants.SSID_DOCUMENT).get().await()
            val listAp = snapshot.toObject(Ssid::class.java)
            emit(DataState.success(listAp))
        } .catch {
            emit(DataState.failed(it.localizedMessage!!))
        } .flowOn(Dispatchers.IO)

    // SET SSID VALUE
    fun setSsid(ssid: Ssid) =
        flow<DataState<Boolean>> {
            emit(DataState.loading())
            val task = dbPosition
                .document(Constants.SSID_DOCUMENT).set(ssid).isSuccessful
            emit(DataState.success(task))
        } .catch {
            emit(DataState.failed(it.localizedMessage!!))
        } .flowOn(Dispatchers.IO)

    // GET & SET K VALUE
    fun getKvalue() =
        flow {
            emit(DataState.loading())
            val task = dbPosition
                .document(Constants.KVAL_DOCUMENT).get().await()
            val kVal = task.toObject(Kvalue::class.java)
            emit(DataState.success(kVal))
        } .catch {
            emit(DataState.failed(it.localizedMessage!!))
        } .flowOn(Dispatchers.IO)

    fun setKvalue(kvalue: Kvalue) =
        flow<DataState<Boolean>> {
            emit(DataState.loading())
            val task = dbPosition.document(Constants.KVAL_DOCUMENT).set(kvalue).isSuccessful
            emit(DataState.success(task))
        } .catch {
            emit(DataState.failed(it.localizedMessage!!))
        } .flowOn(Dispatchers.IO)


}