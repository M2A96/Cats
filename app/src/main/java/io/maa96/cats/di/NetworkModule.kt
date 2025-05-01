package io.maa96.cats.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.maa96.cats.BuildConfig
import io.maa96.cats.data.source.remote.api.CatApi
import io.maa96.cats.util.SecretFields
import java.util.Date
import javax.inject.Singleton
import okhttp3.Authenticator
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * The main [Module] for providing network-related classes
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * provides Gson with custom [Date] converter for [Long] epoch times
     */
    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            // Deserializer to convert json long value into Date
            .registerTypeAdapter(
                Date::class.java,
                JsonDeserializer { json, _, _ ->
                    Date(json.asJsonPrimitive.asLong)
                }
            )
            // Serializer to convert Date value into long json primitive
            .registerTypeAdapter(
                Date::class.java,
                JsonSerializer<Date> { src, _, _ ->
                    JsonPrimitive(src.time)
                }
            )
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }

    /**
     * provides shared [Headers] to be added into [OkHttpClient] instances
     */
    @Singleton
    @Provides
    fun provideSharedHeaders(): Headers {
        return Headers.Builder()
            .add("Accept", "*/*")
            .add("User-Agent", "mobile")
            .build()
    }

    /**
     * Provides [OkHttpClient] instance for token based api services
     *
     * @param headers default shared headers to be added in http request, provided by [provideSharedHeaders]
     * @param authenticator instance of [Authenticator] for handling UNAUTHORIZED errors,
     *
     * @return an instance of [OkHttpClient]
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(
        headers: Headers,
        secretFields: SecretFields
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()

        // if the app is in DEBUG mode OkHttp will show complete log in logcat and Stetho framework
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)

            // Stetho will be initialized here
            builder.addNetworkInterceptor(StethoInterceptor())
        }

        builder.interceptors().add(
            Interceptor { chain ->
                val request = chain.request()
                val requestBuilder = request.newBuilder()
                    // add default shared headers to every http request
                    .headers(headers)
                    // add tokenType and token to Authorization header of request
                    .addHeader(
                        "x-api-key",
                        secretFields.apiKey
                    )
                    .method(request.method, request.body)
                chain.proceed(requestBuilder.build())
            }
        )

        return builder.build()
    }

    /**
     * provide an instance of [Retrofit] for with-token api services
     *
     * @param okHttpClient an instance of with-token [okHttpClient] provided by [provideOkHttpClientWithToken]
     * @param gson an instance of gson provided by [provideGson] to use as retrofit converter factory
     *
     * @return an instance of [Retrofit] for with-token api calls
     */
    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder().client(okHttpClient)
        // create gson converter factory
        .addConverterFactory(GsonConverterFactory.create(gson))
        // get base url from SecretFields interface
        .baseUrl(SecretFields().getBaseUrl())
        .build()

    @Provides
    @Singleton
    fun provideConcreteStarWarsApi(retrofit: Retrofit): CatApi = retrofit.create(CatApi::class.java)
}