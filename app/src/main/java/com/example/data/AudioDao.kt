package com.example.data

import androidx.room.*
import com.example.model.AudioItem
import com.example.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {

    @Query("SELECT * FROM audio_items ORDER BY createdAt DESC")
    fun getAllAudio(): Flow<List<AudioItem>>

    @Query("SELECT * FROM audio_items WHERE isPrivate = 0 ORDER BY createdAt DESC")
    fun getPublicAudio(): Flow<List<AudioItem>>

    @Query("SELECT * FROM audio_items WHERE isPrivate = 1 ORDER BY createdAt DESC")
    fun getPrivateAudio(): Flow<List<AudioItem>>

    @Query("SELECT * FROM audio_items WHERE id = :id")
    suspend fun getById(id: Long): AudioItem?

    @Query("SELECT * FROM audio_items WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: SyncStatus): List<AudioItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AudioItem): Long

    @Update
    suspend fun update(item: AudioItem)

    @Delete
    suspend fun delete(item: AudioItem)

    @Query("DELETE FROM audio_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE audio_items SET syncStatus = :status, syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus, syncedAt: Long?)

    @Query("UPDATE audio_items SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE audio_items SET isPrivate = :isPrivate WHERE id = :id")
    suspend fun setPrivate(id: Long, isPrivate: Boolean)
}
