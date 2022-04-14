package com.aravind.dagger2.networking

import com.aravind.dagger2.questions.Questions
import com.google.gson.annotations.SerializedName

data class QuestionListResponse(@SerializedName("items") val questionsList: List<Questions>)
    