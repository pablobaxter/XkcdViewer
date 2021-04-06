package com.frybits.local.hilt

import android.content.Context
import androidx.room.Room
import com.frybits.local.XkcdDatabase
import com.frybits.local.dao.XkcdDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Singleton
    @Provides
    fun provideXkcdDao(@ApplicationContext context: Context): XkcdDao {
        return Room.databaseBuilder(
            context,
            XkcdDatabase::class.java,
            "xkcd-database"
        ).build().xkcdDao()
    }
}