package com.example.android.politicalpreparedness.di

import android.content.Context
import androidx.room.Room
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.CivicsHttpClient
import com.example.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.inject.Singleton

private const val BASE_URL = "https://www.googleapis.com/civicinfo/v2/"

@Module
@InstallIn(ApplicationComponent::class)
object PersistenceModule {

    @Provides
    @Singleton
    fun provideElectionDatabase(@ApplicationContext appContext: Context): ElectionDatabase {
        return Room
                .databaseBuilder(appContext, ElectionDatabase::class.java, "election_database")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Provides
    @Singleton
    fun provideElectionDao(electionDatabase: ElectionDatabase): ElectionDao {
        return electionDatabase.electionDao
    }

    @Provides
    @Singleton
    fun provideCivicApiService(): CivicsApiService {
        val moshi = Moshi.Builder()
                .add(ElectionAdapter())
                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                .add(KotlinJsonAdapterFactory())
                .build()

        return Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(CivicsHttpClient.getClient())
                .baseUrl(BASE_URL)
                .build()
                .create(CivicsApiService::class.java)
    }
}