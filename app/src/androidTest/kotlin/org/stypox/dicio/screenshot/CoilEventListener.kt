package org.stypox.dicio.screenshot

import android.content.Context
import coil.Coil
import coil.EventListener
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult

class CoilEventListener : EventListener {
    private var startedImages = 0
    private var pendingImages = 0

    override fun onStart(request: ImageRequest) {
        ++startedImages
        ++pendingImages
    }

    override fun onCancel(request: ImageRequest) {
        --pendingImages
    }

    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        --pendingImages
    }

    override fun onError(request: ImageRequest, result: ErrorResult) {
        --pendingImages
    }

    fun setup(context: Context) {
        Coil.setImageLoader(
            ImageLoader.Builder(context)
                .eventListener(this)
                .build()
        )
    }

    fun isIdle(startedAtLeast: Int): Boolean {
        return startedImages >= startedAtLeast && pendingImages == 0
    }

    fun resetStartedImages() {
        startedImages = 0
    }
}
