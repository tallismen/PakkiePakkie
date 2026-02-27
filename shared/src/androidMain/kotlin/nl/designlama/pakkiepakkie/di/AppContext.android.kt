package nl.designlama.pakkiepakkie.di

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context

@SuppressLint("StaticFieldLeak")
actual object AppContext {
    private lateinit var application: Application
    private var currentActivity: Activity? = null

    fun setApplication(context: Context) {
        application = context as Application
    }

    fun setCurrentActivity(activity: Activity) {
        currentActivity = activity
    }

    fun clearCurrentActivity(activity: Activity) {
        if (activity == this.currentActivity) {
            currentActivity = null
        }
    }

    fun getApplication(): Context {
        if (::application.isInitialized.not()) throw Exception("Application context isn't initialized")
        return application.applicationContext
    }

    fun getActivity() = currentActivity
}
