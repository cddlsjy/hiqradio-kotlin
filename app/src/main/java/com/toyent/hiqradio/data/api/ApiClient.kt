package com.toyent.hiqradio.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.util.concurrent.TimeUnit

/**
 * API client singleton
 */
object ApiClient {

    private const val USER_AGENT = "HiqRadio/1.0"
    private const val TIMEOUT_SECONDS = 30L

    // Radio Browser API servers
    private val baseUrls = listOf(
        "https://de1.api.radio-browser.info",
        "https://nl1.api.radio-browser.info",
        "https://us1.api.radio-browser.info"
    )

    private var currentBaseUrlIndex = 0

    private var retrofit: Retrofit? = null
    private var apiService: RadioApiService? = null

    fun getApiService(): RadioApiService {
        if (apiService == null) {
            apiService = getRetrofit().create(RadioApiService::class.java)
        }
        return apiService!!
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = createRetrofit()
        }
        return retrofit!!
    }

    private fun createRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create a trust manager that trusts all certificates
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        // Initialize SSLContext with the trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Connection", "keep-alive")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        return Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getBaseUrl(): String {
        return baseUrls[currentBaseUrlIndex]
    }

    /**
     * Switch to next server if current one fails
     */
    fun switchServer() {
        currentBaseUrlIndex = (currentBaseUrlIndex + 1) % baseUrls.size
        retrofit = null
        apiService = null
    }

    /**
     * Reset to first server
     */
    fun resetServer() {
        currentBaseUrlIndex = 0
        retrofit = null
        apiService = null
    }
}
