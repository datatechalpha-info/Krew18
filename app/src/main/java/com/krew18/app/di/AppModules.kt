package com.krew18.app.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.krew18.app.BuildConfig
import com.krew18.app.data.local.GigDao
import com.krew18.app.data.local.KrewDatabase
import com.krew18.app.data.local.ProfileDao
import com.krew18.app.data.remote.KrewApi
import com.krew18.app.data.remote.KrewRemoteDataSource
import com.krew18.app.data.remote.RetrofitKrewRemoteDataSource
import com.krew18.app.data.remote.fake.FakeKrewRemoteDataSource
import com.krew18.app.data.repository.GigRepository
import com.krew18.app.data.repository.LocalProfileRepository
import com.krew18.app.data.repository.OfflineFirstGigRepository
import com.krew18.app.data.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.time.Clock
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KrewDatabase =
        Room.databaseBuilder(context, KrewDatabase::class.java, KrewDatabase.NAME).build()

    @Provides
    fun provideGigDao(database: KrewDatabase): GigDao = database.gigDao()

    @Provides
    fun provideProfileDao(database: KrewDatabase): ProfileDao = database.profileDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideKrewApi(retrofit: Retrofit): KrewApi = retrofit.create(KrewApi::class.java)

    /**
     * Chooses the backend. The fake one is the default so the demo runs offline; Provider
     * keeps Retrofit from being created at all when it is not used.
     */
    @Provides
    @Singleton
    fun provideRemoteDataSource(
        clock: Clock,
        retrofitDataSource: Provider<RetrofitKrewRemoteDataSource>,
    ): KrewRemoteDataSource =
        if (BuildConfig.USE_FAKE_BACKEND) {
            FakeKrewRemoteDataSource(clock)
        } else {
            retrofitDataSource.get()
        }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindGigRepository(impl: OfflineFirstGigRepository): GigRepository

    @Binds
    abstract fun bindProfileRepository(impl: LocalProfileRepository): ProfileRepository
}
