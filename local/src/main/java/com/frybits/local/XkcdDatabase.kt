package com.frybits.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.frybits.local.dao.XkcdDao
import com.frybits.local.model.XkcdLocalModel

@Database(entities = [XkcdLocalModel::class], version = 1)
abstract class XkcdDatabase : RoomDatabase() {

    abstract fun xkcdDao(): XkcdDao
}