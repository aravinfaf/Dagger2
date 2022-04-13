package com.aravind.dagger2.questions

import com.google.gson.annotations.SerializedName

data class QuestionBody(
    @SerializedName("title") val title: String,
    @SerializedName("question_id") val id: String,
    @SerializedName("body") val body: String,
)
