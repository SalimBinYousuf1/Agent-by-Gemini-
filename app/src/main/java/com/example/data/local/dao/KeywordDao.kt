package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.KeywordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordDao {
    @Query("SELECT * FROM keywords ORDER BY id DESC")
    fun getAllKeywords(): Flow<List<KeywordEntity>>

    @Query("SELECT * FROM keywords WHERE isEnabled = 1")
    suspend fun getEnabledKeywordsSync(): List<KeywordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: KeywordEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(keywords: List<KeywordEntity>)

    @Update
    suspend fun updateKeyword(keyword: KeywordEntity)

    @Delete
    suspend fun deleteKeyword(keyword: KeywordEntity)

    @Query("DELETE FROM keywords WHERE id = :id")
    suspend fun deleteById(id: Long)
}
