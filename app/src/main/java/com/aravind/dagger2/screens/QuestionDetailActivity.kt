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
import com.aravind.dagger2.usecase.QuestionsDetailUseCase
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuestionDetailActivity : AppCompatActivity(), QuestionDetailMvc.Listener {

    private var questionId: String? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var questionDetailMvc: QuestionDetailMvc? = null
    private lateinit var questionsDetailUseCase: QuestionsDetailUseCase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionDetailMvc = QuestionDetailMvc(LayoutInflater.from(this), null)
        setContentView(questionDetailMvc!!.rootView)

        setSupportActionBar(questionDetailMvc?.toolBar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = "Question Detail"
        }

        questionsDetailUseCase = QuestionsDetailUseCase()
        questionDetailMvc?.toolBar?.setNavigationOnClickListener {
            onBackPressed()
        }
        questionId = intent.getStringExtra(EXTRA_QUESTION_ID)
        questionDetailMvc?.swipeRefreshLayout?.isEnabled = false
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

            val response = questionsDetailUseCase.questionsDetailsList(questionId!!)
            when(response){
                is QuestionsDetailUseCase.Result.Success -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        questionDetailMvc?.detailTextview?.text =
                            Html.fromHtml(response.quesId, Html.FROM_HTML_MODE_LEGACY)
                    }//response.body()!!.quesId.question
                    else {
                        questionDetailMvc?.detailTextview?.text = Html.fromHtml(response.quesId)
                    }
                    questionDetailMvc?.hideProgressIndicator()
                }
            }

            try {

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