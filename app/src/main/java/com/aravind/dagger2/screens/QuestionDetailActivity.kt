package com.aravind.dagger2.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aravind.dagger2.Constants
import com.aravind.dagger2.R
import com.aravind.dagger2.mvc.QuestionDetailMvc
import com.aravind.dagger2.networking.StackOverflowApi
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuestionDetailActivity : AppCompatActivity(),QuestionDetailMvc.Listener {

    private var questionId: String? = null
    lateinit var stackOverflowApi: StackOverflowApi
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var questionDetailMvc : QuestionDetailMvc? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionDetailMvc = QuestionDetailMvc(LayoutInflater.from(this),null)
        setContentView(questionDetailMvc!!.rootView)

        setSupportActionBar(questionDetailMvc?.toolBar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = "Question Detail"
        }
        questionDetailMvc?.toolBar?.setNavigationOnClickListener {
            onBackPressed()
        }
        questionId = intent.getStringExtra(EXTRA_QUESTION_ID)
        questionDetailMvc?.swipeRefreshLayout?.isEnabled = false

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = (HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()


        var retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        stackOverflowApi = retrofit.create(StackOverflowApi::class.java)

    }

    companion object {

        const val EXTRA_QUESTION_ID = "EXTRA_QUESTION_ID"

        fun start(context: Context, questionId: String) {
            val intent = Intent(context, QuestionDetailActivity::class.java)
            intent.putExtra(EXTRA_QUESTION_ID, questionId)
            context.startActivity(intent)
        }
    }

    private fun fetchQuestions() {
        coroutineScope.launch {
            questionDetailMvc?.showProgressIndicator()

            try {
                val response = stackOverflowApi.questionDetails(questionId!!)

                if (response.isSuccessful && response.body() != null) {
                    val questionBody = response.body()!!.quesId.title
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        questionDetailMvc?.detailTextview?.text = Html.fromHtml(questionBody,Html.FROM_HTML_MODE_LEGACY)
                    }//response.body()!!.quesId.question
                    else{
                        questionDetailMvc?.detailTextview?.text = Html.fromHtml(questionBody)
                    }
                    questionDetailMvc?.hideProgressIndicator()
                    Log.d("RR","${response.body()!!.quesId.title} Qid $questionId")
                } else {
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                questionDetailMvc?.hideProgressIndicator()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        fetchQuestions()
        questionDetailMvc?.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.coroutineContext.cancelChildren()
        questionDetailMvc?.unRegisterListener(this)
    }

    override fun onBackPress() {

    }

}