package com.aravind.dagger2.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aravind.dagger2.Constants
import com.aravind.dagger2.R
import com.aravind.dagger2.networking.StackOverflowApi
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuestionDetailActivity : AppCompatActivity() {

    private var questionId: String? = null
    lateinit var stackOverflowApi: StackOverflowApi
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var detailTextview: TextView
    private var toolBar : Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)
        swipeRefreshLayout = findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.isEnabled = false
        detailTextview = findViewById(R.id.detailTextview)
        toolBar = findViewById(R.id.toolbar)

        setSupportActionBar(toolBar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = "Question Detail"
        }
        toolBar?.setNavigationOnClickListener {
            onBackPressed()
        }
        questionId = intent.getStringExtra(EXTRA_QUESTION_ID)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = (HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()


        var retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        stackOverflowApi = retrofit.create(StackOverflowApi::class.java)

        fetchQuestions()
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
            showProgressIndicator()

            try {
                val response = stackOverflowApi.questionDetails(questionId!!)

                if (response.isSuccessful && response.body() != null) {
                    val questionBody = response.body()!!.quesId.title
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        detailTextview.text = Html.fromHtml(questionBody,Html.FROM_HTML_MODE_LEGACY)
                    }//response.body()!!.quesId.question
                    else{
                        detailTextview.text = Html.fromHtml(questionBody)
                    }
                    Log.d("RR","${response.body()!!.quesId.title} Qid $questionId")
                } else {
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                hideProgressIndicator()
            }
        }
    }

    private fun hideProgressIndicator() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showProgressIndicator() {
        swipeRefreshLayout.isRefreshing = true
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.coroutineContext.cancelChildren()
    }

}