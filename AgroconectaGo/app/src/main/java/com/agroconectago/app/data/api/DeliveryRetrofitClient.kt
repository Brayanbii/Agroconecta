package com.agroconectago.app.data.api

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object DeliveryRetrofitClient {

    private val cookieStore: MutableMap<String, MutableList<Cookie>> = ConcurrentHashMap()
    private var appPrefs: android.content.SharedPreferences? = null

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies.toMutableList()
            persistCookies()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private fun persistCookies() {
        val prefs = appPrefs ?: return
        val allCookies = cookieStore.values.flatten()
        if (allCookies.isNotEmpty()) {
            val serialized = allCookies.joinToString(";") { "${it.name}=${it.value}" }
            prefs.edit().putString("DELIVERY_COOKIES", serialized).apply()
        }
    }

    fun init(context: Context) {
        appPrefs = context.getSharedPreferences("AgroConectaGoSession", Context.MODE_PRIVATE)
        val saved = appPrefs?.getString("DELIVERY_COOKIES", null)
        if (!saved.isNullOrBlank()) {
            saved.split(";").forEach { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) {
                    val cookie = Cookie.Builder()
                        .name(parts[0].trim())
                        .value(parts[1].trim())
                        .hostOnlyDomain(java.net.URI(ApiConfig.BASE_URL).host)
                        .build()
                    cookieStore.getOrPut(java.net.URI(ApiConfig.BASE_URL).host) { mutableListOf() }.add(cookie)
                }
            }
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                    .addHeader("Accept", "application/json")
                // Solo agregar Content-Type si no es multipart
                if (original.header("Content-Type") == null) {
                    builder.addHeader("Content-Type", "application/json")
                }
                chain.proceed(builder.build())
            }
            .build()
    }

    val api: DeliveryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(DeliveryApiService::class.java)
    }

    // Cliente HTTP compartido (con cookies de sesion) para uploads multipart
    val httpClient: OkHttpClient get() = okHttpClient
}
