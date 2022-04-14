package com.aravind.dagger2.mvc

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aravind.dagger2.R
import com.aravind.dagger2.questions.Questions
import java.util.HashSet

class QuestionListMvc(
     layoutinflater: LayoutInflater,
     parent: ViewGroup?,
) {
    var recyclerView: RecyclerView
    var swipeRefreshLayout: SwipeRefreshLayout
    var adapter: QuestionAdapter? = null

    var rootView: View

    interface Listener {
        fun onRefreshClicked()
        fun onQuestionClickedPosition(questions: Questions)
    }

    init {
        rootView = layoutinflater.inflate(R.layout.activity_questions, parent, false)
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefresh)
        recyclerView = rootView.findViewById(R.id.recyclerview)

        swipeRefreshLayout.setOnRefreshListener {
            for (listener in listeners){
                listener.onRefreshClicked()
            }
        }
        recyclerView.layoutManager =
            LinearLayoutManager(rootView.context, LinearLayoutManager.VERTICAL, false)

        adapter = QuestionAdapter { clickedQuestion ->
            for (listener in listeners){
                listener.onQuestionClickedPosition(clickedQuestion)
            }
        }
    }
    private val listeners = HashSet<Listener>()

    fun registerListener(listener: Listener) {
        listeners.add(listener)
    }

    fun unRegisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun hideProgressIndicator() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    fun showProgressIndicator() {
        swipeRefreshLayout.isRefreshing = true
    }

    fun bindQuestions(questionList : List<Questions>){
        adapter?.bindData(questionList)
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
}

