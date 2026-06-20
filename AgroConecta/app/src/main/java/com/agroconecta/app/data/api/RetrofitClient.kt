package com.agroconecta.app.data.api

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

object RetrofitClient {

    private var cookieJar = object : CookieJar {
        private val store: MutableMap<String, MutableList<Cookie>> = ConcurrentHashMap()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            store[url.host] = cookies.toMutableList()
            persistCookies()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return store[url.host] ?: emptyList()
        }

        fun getAllCookies(): List<Cookie> = store.values.flatten()

        fun loadCookie(host: String, cookie: Cookie) {
            store.getOrPut(host) { mutableListOf() }.add(cookie)
        }
    }

    private var appPrefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        appPrefs = context.getSharedPreferences("AgroConectaSession", Context.MODE_PRIVATE)
        val saved = appPrefs?.getString("COOKIES", null)
        if (!saved.isNullOrBlank()) {
            for (line in saved.split("\n")) {
                val kv = line.split("=", limit = 2)
                if (kv.size == 2) {
                    try {
                        val cookie = Cookie.Builder()
                            .name(kv[0])
                            .value(kv[1])
                            .domain(java.net.URI(ApiConfig.BASE_URL).host)
                            .path("/")
                            .build()
                        cookieJar.loadCookie(java.net.URI(ApiConfig.BASE_URL).host, cookie)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun persistCookies() {
        if (appPrefs == null) return
        val cookies = cookieJar.getAllCookies()
        val data = cookies.joinToString("\n") { "${it.name}=${it.value}" }
        appPrefs?.edit()?.putString("COOKIES", data)?.apply()
    }

    private val BASE_URL get() = ApiConfig.BASE_URL

    private val gson = GsonBuilder().setLenient().create()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val usuarioApiService: UsuarioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(UsuarioApiService::class.java)
    }
}
