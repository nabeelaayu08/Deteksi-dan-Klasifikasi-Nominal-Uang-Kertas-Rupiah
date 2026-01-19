package com.example.beraparupiah.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.beraparupiah.data.database.AppDatabase
import com.example.beraparupiah.data.database.RiwayatRepository
import com.example.beraparupiah.data.model.RiwayatDeteksi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DeteksiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RiwayatRepository
    val allRiwayat: LiveData<List<RiwayatDeteksi>>

    init {
        val riwayatDao = AppDatabase.getDatabase(application).riwayatDao()
        repository = RiwayatRepository(riwayatDao)
        allRiwayat = repository.allRiwayat
    }

    fun saveDetection(nominal: String, confidence: Float, imagePath: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val riwayat = RiwayatDeteksi(
            userId = userId,
            nominal = nominal,
            confidence = confidence,
            timestamp = System.currentTimeMillis(),
            imagePath = imagePath
        )

        viewModelScope.launch {
            repository.insert(riwayat)
        }
    }

    fun getRiwayatByUser(userId: String): LiveData<List<RiwayatDeteksi>> {
        return repository.getRiwayatByUser(userId)
    }

    fun deleteRiwayat(riwayat: RiwayatDeteksi) {
        viewModelScope.launch {
            repository.delete(riwayat)
        }
    }

    fun clearAllRiwayat() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // Delete multiple items
    fun deleteMultiple(items: List<RiwayatDeteksi>) {
        viewModelScope.launch {
            repository.deleteAll(items)
        }
    }

    private fun RiwayatRepository.deleteAll(items: kotlin.collections.List<com.example.beraparupiah.data.model.RiwayatDeteksi>) {}
}