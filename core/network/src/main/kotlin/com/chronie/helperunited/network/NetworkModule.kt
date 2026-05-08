package com.chronie.helperunited.network

import com.chronie.helperunited.base.AccountRegion
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network dependency injection module.
 * Provides Retrofit, OkHttp, and Moshi instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    /**
     * Creates a Retrofit instance for a specific region.
     */
    private fun createRetrofit(
        baseUrl: String,
        cookie: String,
        region: AccountRegion,
        deviceId: String,
        moshi: Moshi,
        baseClient: OkHttpClient,
    ): Retrofit {
        val client = baseClient.newBuilder()
            .addInterceptor(HoYoAuthInterceptor(cookie, region, deviceId))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Provides HoYoRecordApi for a specific account's region.
     * Note: For multi-account support, use [HoYoApiFactory] instead.
     */
    @Provides
    @Singleton
    fun provideHoYoApiFactory(
        moshi: Moshi,
        baseClient: OkHttpClient,
    ): HoYoApiFactory = HoYoApiFactory(moshi, baseClient)
}

/**
 * Factory for creating per-account API service instances.
 * Each account may have a different region, cookie, and API host.
 */
class HoYoApiFactory(
    private val moshi: Moshi,
    private val baseClient: OkHttpClient,
) {
    fun createRecordApi(
        region: AccountRegion,
        cookie: String,
        deviceId: String = "",
    ): HoYoRecordApi {
        val host = HoYoApiConfig.recordApiHost(region)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://$host/")
            .client(
                baseClient.newBuilder()
                    .addInterceptor(HoYoAuthInterceptor(cookie, region, deviceId))
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(HoYoRecordApi::class.java)
    }

    fun createEnkaApi(): EnkaApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://enka.network/")
            .client(baseClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(EnkaApi::class.java)
    }
}
