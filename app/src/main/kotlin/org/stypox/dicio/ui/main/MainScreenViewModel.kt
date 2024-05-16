package org.stypox.dicio.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dicio.skill.SkillContext
import org.stypox.dicio.eval.SkillEvaluator2
import org.stypox.dicio.io.input.InputEventsModule
import org.stypox.dicio.io.input.SttInputDevice
import org.stypox.dicio.skills.SkillHandler2

@HiltViewModel(
    assistedFactory = MainScreenViewModel.Factory::class
)
class MainScreenViewModel @AssistedInject constructor(
    @Assisted("requestPermissions") requestPermissions: suspend (Array<String>) -> Boolean,
    application: Application,
    val skillContext: SkillContext,
    skillHandler: SkillHandler2,
    val inputEventsModule: InputEventsModule,
    val sttInputDevice: SttInputDevice?,
) : AndroidViewModel(application) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("requestPermissions") requestPermissions: suspend (Array<String>) -> Boolean,
        ): MainScreenViewModel
    }

    val skillEvaluator = SkillEvaluator2(
        inputEventsModule = inputEventsModule,
        skillContext = skillContext,
        skillHandler = skillHandler,
        requestPermissions = requestPermissions,
    )
}
