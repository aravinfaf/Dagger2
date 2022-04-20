package com.aravind.dagger2.mvc

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aravind.dagger2.R
import com.aravind.dagger2.base.BaseView

class QuestionDetailMvc(
    layoutInflator: LayoutInflater,
    parent: ViewGroup?
) : BaseView<QuestionDetailMvc.Listener>(
    layoutInflator,
    parent,
    R.layout.activity_question_detail
) {

    var swipeRefreshLayout: SwipeRefreshLayout
    var detailTextview: TextView
    var toolBar : Toolbar? = null

    interface Listener {
        fun onBackPress()
    }

    init {
        swipeRefreshLayout = rootView. findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.isEnabled = false
        detailTextview = rootView.findViewById(R.id.detailTextview)
        toolBar = rootView.findViewById(R.id.toolbar)

        toolBar?.setOnClickListener {
            for (listener in listeners){
                listener.onBackPress()
            }
        }
    }

     fun hideProgressIndicator() {
        if (swipeRefreshLayout!!.isRefreshing) {
            swipeRefreshLayout?.isRefreshing = false
        }
    }

     fun showProgressIndicator() {
        swipeRefreshLayout?.isRefreshing = true
    }
}