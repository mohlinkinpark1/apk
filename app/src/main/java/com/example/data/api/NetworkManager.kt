package com.example.data.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class NetworkManager(context: Context) {

    private val sharedPrefs = context.getSharedPreferences("rental_admin_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_BASE_URL = "base_url"
        const val KEY_ADMIN_TOKEN = "admin_token"
        
        const val DEFAULT_BASE_URL = "https://ais-pre-cmdkwhljtfrdbhkwordzxm-511132081774.europe-west2.run.app"
        const val DEFAULT_ADMIN_TOKEN = "supersecretadmintoken2024"
    }

    var baseUrl: String
        get() {
            var url = sharedPrefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
            if (!url.endsWith("/")) {
                url += "/"
            }
            return url
        }
        set(value) {
            val url = if (value.endsWith("/")) value else "$value/"
            sharedPrefs.edit().putString(KEY_BASE_URL, url).apply()
            rebuildService()
        }

    var adminToken: String
        get() = sharedPrefs.getString(KEY_ADMIN_TOKEN, DEFAULT_ADMIN_TOKEN) ?: DEFAULT_ADMIN_TOKEN
        set(value) {
            sharedPrefs.edit().putString(KEY_ADMIN_TOKEN, value).apply()
        }

    private var apiService: RentalApiService? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    init {
        rebuildService()
    }

    fun rebuildService() {
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            apiService = retrofit.create(RentalApiService::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            apiService = null
        }
    }

    fun getService(): RentalApiService {
        if (apiService == null) {
            rebuildService()
        }
        return apiService ?: throw IllegalStateException("API Service not initialized. Check your Base URL.")
    }
}
