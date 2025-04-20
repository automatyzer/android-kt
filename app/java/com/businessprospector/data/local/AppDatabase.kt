package com.businessprospector.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.businessprospector.data.local.converter.DateConverter
import com.businessprospector.data.local.converter.JsonConverter
import com.businessprospector.data.local.dao.AnalyticsDao
import com.businessprospector.data.local.dao.ContactDao
import com.businessprospector.data.local.dao.MessageDao
import com.businessprospector.data.local.dao.SequenceDao
import com.businessprospector.data.local.entity.AnalyticsEntity
import com.businessprospector.data.local.entity.ContactEntity
import com.businessprospector.data.local.entity.MessageEntity
import com.businessprospector.data.local.entity.SequenceEntity
import com.businessprospector.data.local.entity.SequenceStepEntity
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        ContactEntity::class,
        MessageEntity::class,
        SequenceEntity::class,
        SequenceStepEntity::class,
        AnalyticsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, JsonConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao
    abstract fun sequenceDao(): SequenceDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        private const val DATABASE_NAME = "business_prospector.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, encryptionKey: ByteArray): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    // Zastosowanie szyfrowania bazy danych za pomocą SQLCipher
                    .openHelperFactory(SupportFactory(SQLiteDatabase.getBytes(encryptionKey)))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}