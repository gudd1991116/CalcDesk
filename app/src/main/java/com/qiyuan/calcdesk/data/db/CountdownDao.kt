package com.qiyuan.calcdesk.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownDao {
    @Query("SELECT * FROM countdowns")
    fun observeAll(): Flow<List<CountdownEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CountdownEntity): Long

    @Delete
    suspend fun delete(item: CountdownEntity)

    @Query("DELETE FROM countdowns WHERE id = :id")
    suspend fun deleteById(id: Long)
}
