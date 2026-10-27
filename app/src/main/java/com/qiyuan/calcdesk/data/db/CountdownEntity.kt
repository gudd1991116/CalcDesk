package com.qiyuan.calcdesk.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countdowns")
data class CountdownEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** ISO local date `yyyy-MM-dd`. */
    val targetDate: String,
    val createdAt: Long = System.currentTimeMillis(),
)
