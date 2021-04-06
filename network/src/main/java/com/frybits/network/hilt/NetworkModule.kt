package com.frybits.network.hilt

import com.frybits.network.XkcdNetwork
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideXkcdNetwork(): XkcdNetwork {
        return Retrofit.Builder()
            .baseUrl("https://www.xkcd.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XkcdNetwork::class.java)
    }
}