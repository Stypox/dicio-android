package org.dicio.skill.util

interface CleanableUp {
    /**
     * Stop anything this object is doing, dispose disposables, detach listeners, set references to
     * external objects to `null`, release resources, etc...
     */
    fun cleanup()
}
