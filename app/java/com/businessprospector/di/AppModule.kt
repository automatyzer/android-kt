package com.businessprospector.di

import android.content.Context
import androidx.room.Room
import com.businessprospector.data.local.AppDatabase
import com.businessprospector.data.local.dao.AnalyticsDao
import com.businessprospector.data.local.dao.ContactDao
import com.businessprospector.data.local.dao.MessageDao
import com.businessprospector.data.local.dao.SequenceDao
import com.businessprospector.data.remote.api.GoogleSearchApi
import com.businessprospector.data.remote.api.LlmApi
import com.businessprospector.domain.service.EncryptionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideEncryptionService(@ApplicationContext context: Context): EncryptionService {
        return EncryptionService(context)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        encryptionService: EncryptionService
    ): AppDatabase {
        return AppDatabase.getInstance(context, encryptionService.getDatabaseEncryptionKey())
    }

    @Singleton
    @Provides
    fun provideContactDao(appDatabase: AppDatabase): ContactDao {
        return appDatabase.contactDao()
    }

    @Singleton
    @Provides
    fun provideMessageDao(appDatabase: AppDatabase): MessageDao {
        return appDatabase.messageDao()
    }

    @Singleton
    @Provides
    fun provideSequenceDao(appDatabase: AppDatabase): SequenceDao {
        return appDatabase.sequenceDao()
    }

    @Singleton
    @Provides
    fun provideAnalyticsDao(appDatabase: AppDatabase): AnalyticsDao {
        return appDatabase.analyticsDao()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideGoogleSearchApi(okHttpClient: OkHttpClient): GoogleSearchApi {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/customsearch/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleSearchApi::class.java)
    }

    @Singleton
    @Provides
    fun provideLlmApi(okHttpClient: OkHttpClient): LlmApi {
        // Base URL może być zmieniony w zależności od używanego modelu
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LlmApi::class.java)
    }
}