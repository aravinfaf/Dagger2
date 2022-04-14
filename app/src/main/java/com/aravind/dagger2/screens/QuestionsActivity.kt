package com.aravind.dagger2.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aravind.dagger2.Constants
import com.aravind.dagger2.R
import com.aravind.dagger2.mvc.QuestionListMvc
import com.aravind.dagger2.networking.StackOverflowApi
import com.aravind.dagger2.questions.Questions
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.ArrayList


class QuestionsActivity : AppCompatActivity(), QuestionListMvc.Listener {

   private var isDataLoad = false
    lateinit var stackOverflowApi: StackOverflowApi

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var toolBar : Toolbar? = null

    lateinit var questionListMvc : QuestionListMvc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionListMvc = QuestionListMvc(LayoutInflater.from(this),null)
        setContentView(questionListMvc.rootView)

        toolBar = findViewById(R.id.toolbar)

        setSupportActionBar(toolBar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = "Question list"
        }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = (HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()


        var retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
         stackOverflowApi = retrofit.create(StackOverflowApi::class.java)
    }

    private fun fetchQuestions() {
        coroutineScope.launch {
            questionListMvc.showProgressIndicator()

            try {
                val response = stackOverflowApi.lastActiveQuestions(20)

                if (response.isSuccessful && response != null){
                    questionListMvc.bindQuestions(response.body()!!.questionsList)
                    questionListMvc.recyclerView.adapter = questionListMvc.adapter
                    isDataLoad = true

                    response.body()!!.questionsList.forEach {
                        Log.d("Q",it.title)
                    }
                }
                else{
                    Toast.makeText(applicationContext,"Error",Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                questionListMvc.hideProgressIndicator()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        questionListMvc.registerListener(this)
        if (!isDataLoad) {
            fetchQuestions()
        }
    }

    override fun onStop() {
        super.onStop()
        questionListMvc.unRegisterListener(this)
        coroutineScope.coroutineContext.cancelChildren()
    }

    override fun onRefreshClicked() {
        fetchQuestions()
    }

    override fun onQuestionClickedPosition(questions: Questions) {
        QuestionDetailActivity.start(this,questions.id)
    }

}