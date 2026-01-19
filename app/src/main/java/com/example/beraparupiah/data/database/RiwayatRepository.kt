package com.example.beraparupiah.data.database

import androidx.lifecycle.LiveData
import com.example.beraparupiah.data.model.RiwayatDeteksi

class RiwayatRepository(private val riwayatDao: RiwayatDao) {

    val allRiwayat: LiveData<List<RiwayatDeteksi>> = riwayatDao.getAllRiwayat()

    fun getRiwayatByUser(userId: String): LiveData<List<RiwayatDeteksi>> {
        return riwayatDao.getRiwayatByUser(userId)
    }

    suspend fun insert(riwayat: RiwayatDeteksi) {
        riwayatDao.insert(riwayat)
    }

    suspend fun update(riwayat: RiwayatDeteksi) {
        riwayatDao.update(riwayat)
    }

    suspend fun delete(riwayat: RiwayatDeteksi) {
        riwayatDao.delete(riwayat)
    }

    suspend fun deleteAll() {
        riwayatDao.deleteAll()
    }

    suspend fun getRiwayatById(id: Int): RiwayatDeteksi? {
        return riwayatDao.getRiwayatById(id)
    }

    // Delete multiple items
    suspend fun deleteAll(items: List<RiwayatDeteksi>) {
        riwayatDao.deleteAll(items)
    }
}

private fun RiwayatDao.deleteAll(items: kotlin.collections.List<com.example.beraparupiah.data.model.RiwayatDeteksi>) {}
