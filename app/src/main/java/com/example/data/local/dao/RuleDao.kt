package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RuleAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rule_apps ORDER BY addedTimestamp DESC")
    fun getAllApps(): Flow<List<RuleAppEntity>>

    @Query("SELECT * FROM rule_apps WHERE isEnabled = 1")
    suspend fun getEnabledAppsSync(): List<RuleAppEntity>

    @Query("SELECT * FROM rule_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): RuleAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: RuleAppEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(apps: List<RuleAppEntity>)

    @Update
    suspend fun updateApp(app: RuleAppEntity)

    @Delete
    suspend fun deleteApp(app: RuleAppEntity)

    @Query("DELETE FROM rule_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)
}
