package com.aravind.dagger2.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.aravind.dagger2.R
import com.aravind.dagger2.mvc.QuestionListMvc
import com.aravind.dagger2.questions.Questions
import com.aravind.dagger2.usecase.QuestionsUseCase
import kotlinx.coroutines.*


class QuestionsActivity : AppCompatActivity(), QuestionListMvc.Listener {

   private var isDataLoad = false

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var toolBar : Toolbar? = null

    lateinit var questionListMvc : QuestionListMvc
    lateinit var questionlistusecase: QuestionsUseCase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionListMvc = QuestionListMvc(LayoutInflater.from(this),null)
        setContentView(questionListMvc.rootView)

        toolBar = findViewById(R.id.toolbar)
        questionlistusecase = QuestionsUseCase()
        setSupportActionBar(toolBar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = "Question list"
        }
    }

    private fun fetchQuestions() {
        coroutineScope.launch {
            questionListMvc.showProgressIndicator()

            try {
                val result = questionlistusecase.fetchQuestionListUseCase()
                when(result){
                   is QuestionsUseCase.Result.Success -> {
                        questionListMvc.bindQuestions(result.questionsList)
                       questionListMvc.recyclerView.adapter = questionListMvc.adapter
                       isDataLoad = true
                    }
                    is QuestionsUseCase.Result.Error -> {
                        Toast.makeText(applicationContext,"Error",Toast.LENGTH_LONG).show()
                    }
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