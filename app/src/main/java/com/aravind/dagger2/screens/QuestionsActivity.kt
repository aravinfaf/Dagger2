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
import com.aravind.dagger2.networking.StackOverflowApi
import com.aravind.dagger2.questions.Questions
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.ArrayList


class QuestionsActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isDataLoad = false
    lateinit var stackOverflowApi: StackOverflowApi
    private var adapter : QuestionAdapter? = null

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var toolBar : Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)

        toolBar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefresh)
        recyclerView = findViewById(R.id.recyclerview)

        setSupportActionBar(toolBar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = "Question list"
        }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = QuestionAdapter { clickedQuestion ->
            QuestionDetailActivity.start(this,clickedQuestion.id)
        }
        swipeRefreshLayout.setOnRefreshListener {
            fetchQuestions()
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
            showProgressIndicator()

            try {
                val response = stackOverflowApi.lastActiveQuestions(20)

                if (response.isSuccessful && response != null){
                    adapter?.bindData(response.body()!!.questionsList)
                    recyclerView.adapter = adapter
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

    override fun onStart() {
        super.onStart()
        if (!isDataLoad) {
            fetchQuestions()
        }
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.coroutineContext.cancelChildren()
    }
}

class QuestionAdapter(
    val QuestionClickListener : (Questions) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    private var list: List<Questions> = ArrayList(0)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.text_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_layout, null, false)
        )
    }

    override fun onBindViewHolder(holder: QuestionAdapter.ViewHolder, position: Int) {
        holder.textTitle.text = list[position].title
        Log.d("Title","${list[position].title}")

        holder.textTitle.setOnClickListener {
            QuestionClickListener.invoke(list[position])
        }
    }

    override fun getItemCount(): Int = list.size

    fun bindData(questionList: List<Questions>) {
        this.list = questionList
        Log.d("SSss","${questionList.size}")
        notifyDataSetChanged()
    }
}