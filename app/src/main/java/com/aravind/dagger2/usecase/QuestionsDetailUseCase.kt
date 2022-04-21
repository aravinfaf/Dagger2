package com.aravind.dagger2.usecase

import android.util.Log
import com.aravind.dagger2.Constants
import com.aravind.dagger2.networking.StackOverflowApi
import com.aravind.dagger2.questions.QuestionBody
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuestionsDetailUseCase {
    sealed class Result{
        class Success(val quesId: String) : Result()
        object Error : Result()
    }

    var retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    var stackOverflowApi: StackOverflowApi =  retrofit.create(StackOverflowApi::class.java)

    suspend fun questionsDetailsList(questionId : String) : Result{

        return withContext(Dispatchers.IO){
            val response = stackOverflowApi.questionDetails(questionId)
            try {
                if (response.isSuccessful && response.body() != null) {
                    Log.e("DDD","DDD ${response.body()!!.questions[0].body}")
                    return@withContext Result.Success(response.body()!!.questions[0].body)
                } else {
                    return@withContext Result.Error
                }
            }catch (e : Exception){
                if (e !is CancellationException){
                    return@withContext Result.Error
                }else{
                    throw e
                }
            }
        }
    }
}