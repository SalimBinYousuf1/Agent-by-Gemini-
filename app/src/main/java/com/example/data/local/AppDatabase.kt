package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.KeywordDao
import com.example.data.local.dao.RuleDao
import com.example.data.local.dao.TriggerLogDao
import com.example.data.local.entity.KeywordEntity
import com.example.data.local.entity.RuleAppEntity
import com.example.data.local.entity.TriggerLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RuleAppEntity::class,
        KeywordEntity::class,
        TriggerLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun keywordDao(): KeywordDao
    abstract fun triggerLogDao(): TriggerLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salim_accountability.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            populateInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val defaultApps = listOf(
                RuleAppEntity("com.instagram.android", "Instagram", "social"),
                RuleAppEntity("com.zhiliaoapp.musically", "TikTok", "social"),
                RuleAppEntity("com.twitter.android", "X (Twitter)", "social"),
                RuleAppEntity("com.facebook.katana", "Facebook", "social"),
                RuleAppEntity("com.reddit.frontpage", "Reddit", "social"),
                RuleAppEntity("com.google.android.youtube", "YouTube Shorts", "entertainment")
            )
            database.ruleDao().insertAll(defaultApps)

            val defaultKeywords = listOf(
                KeywordEntity(keyword = "casino", category = "gambling", severity = "HIGH"),
                KeywordEntity(keyword = "betting", category = "gambling", severity = "HIGH"),
                KeywordEntity(keyword = "slots", category = "gambling", severity = "HIGH"),
                KeywordEntity(keyword = "porn", category = "adult", severity = "HIGH"),
                KeywordEntity(keyword = "xxx", category = "adult", severity = "HIGH"),
                KeywordEntity(keyword = "infinite scroll", category = "distraction", severity = "LOW"),
                KeywordEntity(keyword = "endless feed", category = "distraction", severity = "LOW")
            )
            database.keywordDao().insertAll(defaultKeywords)
        }
    }
}
