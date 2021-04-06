package com.frybits.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.frybits.local.model.XkcdLocalModel

@Dao
interface XkcdDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComics(xkcdLocalModels: List<XkcdLocalModel>)

    @Delete
    suspend fun deleteComic(xkcdLocalModel: XkcdLocalModel)

    @Query("SELECT * FROM xkcdlocalmodel")
    fun pagingSource(): PagingSource<Int, XkcdLocalModel>

    @Query("DELETE FROM xkcdlocalmodel")
    suspend fun clearAll()
}