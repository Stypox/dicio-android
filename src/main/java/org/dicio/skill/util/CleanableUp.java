package org.dicio.skill.util;

public interface CleanableUp {
    /**
     * Stop anything this object is doing, dispose disposables, detach listeners, set references to
     * external objects to {@code null}, release resources, etc...
     */
    void cleanup();
}
