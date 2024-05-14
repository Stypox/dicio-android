package org.stypox.dicio.util

import androidx.annotation.StringRes
import org.dicio.skill.SkillContext

fun SkillContext.getString(@StringRes resId: Int): String {
    return this.android?.getString(resId) ?: ""
}

fun SkillContext.getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
    return this.android?.getString(resId, *formatArgs) ?: ""
}
