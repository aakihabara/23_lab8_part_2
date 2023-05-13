package com.bignerdranch.android.auto

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
data class Auto (@PrimaryKey
    val id: UUID = UUID.randomUUID(),
                 var mark: String = "",
                 var model: String = "",
                 var price: Int = 0)
{

}