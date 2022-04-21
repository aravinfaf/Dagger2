package com.aravind.dagger2.usecase

import com.aravind.dagger2.Constants
import com.aravind.dagger2.networking.StackOverflowApi
import com.aravind.dagger2.questions.Questions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuestionsUseCase {
    sealed class Result {
        class Success(val questionsList: List<Questions>) : Result()
        object Error : Result()
    }

    val client: OkHttpClient = OkHttpClient.Builder().build()
    var retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    var stackOverflowApi: StackOverflowApi = retrofit.create(StackOverflowApi::class.java)

    suspend fun fetchQuestionListUseCase(): Result {
        return withContext(Dispatchers.IO) {
            val response = stackOverflowApi.lastActiveQuestions(20)
            try {
                if (response.isSuccessful && response.body() != null) {
                    return@withContext Result.Success(response.body()!!.questionsList)
                } else {
                    return@withContext Result.Error
                }
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    return@withContext Result.Error
                } else {
                    throw t
                }
            }
        }
    }
}