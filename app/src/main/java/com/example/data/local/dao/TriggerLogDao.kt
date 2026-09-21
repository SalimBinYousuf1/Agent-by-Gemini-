package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.TriggerLogEntity
import kotlinx.coroutines.flow.Flow

data class CategoryStat(
    val category: String,
    val count: Int
)

@Dao
interface TriggerLogDao {
    @Query("SELECT * FROM trigger_logs ORDER BY firedAt DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<TriggerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TriggerLogEntity): Long

    @Query("SELECT COUNT(*) FROM trigger_logs WHERE firedAt >= :timestamp")
    fun getCountSince(timestamp: Long): Flow<Int>

    @Query("SELECT category, COUNT(*) as count FROM trigger_logs WHERE firedAt >= :timestamp GROUP BY category ORDER BY count DESC")
    fun getStatsByCategorySince(timestamp: Long): Flow<List<CategoryStat>>

    @Query("DELETE FROM trigger_logs")
    suspend fun clearAll()
}
