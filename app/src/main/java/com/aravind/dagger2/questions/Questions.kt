package com.aravind.dagger2.questions

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Questions(
    @SerializedName("title") val title: String,
    @SerializedName("question_id") val id: String
)
