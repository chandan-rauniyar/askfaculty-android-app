package com.chandan.askfaculty.di

import android.util.Log
import com.chandan.askfaculty.BuildConfig
import com.chandan.askfaculty.data.remote.ApiService
import com.chandan.askfaculty.data.remote.AuthApiService
import com.chandan.askfaculty.data.remote.HealthApiService
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Log.d("OkHttpNetwork", message)
            }.apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    @Named("root")
    fun provideRootRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val rawBase = BuildConfig.API_BASE_URL.removeSuffix("/")
        val rootBase = if (rawBase.endsWith("/api/v1")) {
            rawBase.removeSuffix("/api/v1") + "/"
        } else {
            rawBase + "/"
        }

        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(rootBase)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Named("api")
    fun provideApiRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val rawBase = BuildConfig.API_BASE_URL.removeSuffix("/")
        val apiBase = if (rawBase.endsWith("/api/v1")) {
            rawBase + "/"
        } else {
            rawBase + "/api/v1/"
        }

        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(apiBase)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideHealthApiService(@Named("root") rootRetrofit: Retrofit): HealthApiService {
        return rootRetrofit.create(HealthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiService(@Named("api") apiRetrofit: Retrofit): ApiService {
        return apiRetrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("api") apiRetrofit: Retrofit): AuthApiService {
        return apiRetrofit.create(AuthApiService::class.java)
    }
}
