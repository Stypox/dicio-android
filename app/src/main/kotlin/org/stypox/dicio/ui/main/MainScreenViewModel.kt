package org.stypox.dicio.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dicio.skill.SkillContext
import org.stypox.dicio.eval.SkillEvaluator2
import org.stypox.dicio.io.input.InputEventsModule
import org.stypox.dicio.io.input.SttInputDevice
import org.stypox.dicio.skills.SkillHandler2
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
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
