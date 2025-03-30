package org.stypox.dicio.di

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityForResultManager @Inject constructor() {
    private val launchers: ArrayList<WeakReference<ActivityResultLauncher<Intent>>> = ArrayList()
    private var callback: ((ActivityResult) -> Unit)? = null

    fun addLauncher(activity: ComponentActivity): ActivityResultLauncher<Intent> {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::handleResult
        )

        launchers.add(WeakReference(launcher))
        return launcher
    }

    fun launch(input: Intent, newCallback: (ActivityResult) -> Unit): Boolean {
        val launcher = synchronized(launchers) {
            launchers.removeAll { it.get() == null }
            launchers.map { it.get() }.firstOrNull { it != null }
        } ?: return false

        synchronized(this) {
            if (callback == null) {
                callback = newCallback
            } else {
                throw IllegalArgumentException("An activity for result request is already active")
            }
        }

        launcher.launch(input)
        return true
    }

    private fun handleResult(activityResult: ActivityResult) {
        synchronized(this) {
            callback?.invoke(activityResult)
            callback = null
        }
    }
}
