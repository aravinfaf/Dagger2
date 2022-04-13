package com.aravind.dagger2.networking

import com.aravind.dagger2.questions.QuestionBody
import com.aravind.dagger2.questions.Questions
import com.google.gson.annotations.SerializedName

data class SingleQuestionResponse(@SerializedName("items") val questions: List<QuestionBody>) {
    val quesId: QuestionBody get() = questions[0]
}