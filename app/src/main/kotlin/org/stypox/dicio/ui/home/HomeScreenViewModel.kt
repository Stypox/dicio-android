package org.stypox.dicio.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dicio.skill.context.SkillContext
import org.stypox.dicio.eval.SkillEvaluator2
import org.stypox.dicio.eval.SkillHandler2
import org.stypox.dicio.io.input.InputEventsModule
import org.stypox.dicio.io.input.SttInputDevice
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    application: Application,
    val skillContext: SkillContext,
    skillHandler: SkillHandler2,
    val inputEventsModule: InputEventsModule,
    val sttInputDevice: SttInputDevice?,
) : AndroidViewModel(application) {

    val skillEvaluator = SkillEvaluator2(
        scope = viewModelScope,
        skillContext = skillContext,
        skillHandler = skillHandler,
        inputEventsModule = inputEventsModule,
        sttInputDevice = sttInputDevice,
    )
}
