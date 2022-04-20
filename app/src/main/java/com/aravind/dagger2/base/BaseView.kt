package com.aravind.dagger2.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.aravind.dagger2.mvc.QuestionListMvc
import java.util.HashSet

open class BaseView<LISTENER_TYPE>(
    layoutInflater : LayoutInflater,
    parent : ViewGroup?,
    @LayoutRes layoutId : Int
) {
     val rootView: View = layoutInflater.inflate(layoutId, parent, false)

    val listeners = HashSet<LISTENER_TYPE>()

    fun registerListener(listener: LISTENER_TYPE) {
        listeners.add(listener)
    }

    fun unRegisterListener(listener: LISTENER_TYPE) {
        listeners.remove(listener)
    }
}